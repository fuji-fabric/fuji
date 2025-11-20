package mod.fuji.module.initializer.world_downloader;

import com.google.common.collect.EvictingQueue;
import com.sun.net.httpserver.HttpServer;
import mod.fuji.core.annotation.Unused;
import mod.fuji.core.auxiliary.ExceptionUtil;
import mod.fuji.core.auxiliary.RandomUtil;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.IOUtil;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.world_downloader.config.model.WorldDownloaderConfigModel;
import mod.fuji.module.initializer.world_downloader.structure.FileDownloadHandler;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Document(id = 1751826613773L, value = """
    Allows a player to download nearest `region` file.

    It's useful for players to save and debug a `redstone structure`.
    It simply downloads the `identical` region file used by the server.
    Simple, no surprises.
    It's also useful for players to download other's buildings, or their own buildings.
    """)
public class WorldDownloaderInitializer extends ModuleInitializer {

    private static final double BYTE_TO_MEGABYTE = 1.0 * 1024 * 1024;
    private static final BaseConfigurationHandler<WorldDownloaderConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, WorldDownloaderConfigModel.class);

    private static EvictingQueue<String> downloadContextQueue;
    private static HttpServer httpServer;

    private static void restartHttpServer() {
        /* If the server already started, stop it first. */
        if (httpServer != null) {
            httpServer.stop(0);
        }

        /* Create a new http server instance. */
        try {
            httpServer = HttpServer.create(new InetSocketAddress(config.model().port), 0);
            httpServer.start();
        } catch (IOException e) {
            LogUtil.error("Failed to start http server.", e);
        }
    }

    public static void safelyRemoveContext(String path) {
        try {
            httpServer.removeContext(path);
        } catch (IllegalArgumentException ignored) {
            // Just continue.
        }
    }

    @Document(id = 1751826617256L, value = "Download the region file around you.")
    @CommandNode("download")
    private static int $download(@CommandSource ServerPlayer player) {
        /* Ensure the download server is set up. */
        if (httpServer == null) {
            restartHttpServer();
        }

        /* Ensure the max simultaneous download count. */
        if (downloadContextQueue.remainingCapacity() == 0) {
            LogUtil.debug("Contexts is full, remove the oldest context. {}", downloadContextQueue.peek());
            safelyRemoveContext(downloadContextQueue.poll());
        }

        /* Make url and path. */
        String url = config.model().url_format;
        int port = config.model().port;
        url = url.replace("%port%", String.valueOf(port));
        String path = "/world-download/" + RandomUtil.randomUUID();
        url = url.replace("%path%", path);
        downloadContextQueue.add(path);

        /* Make a new download context. */
        File file = compressRegionFile(player);
        TextHelper.sendBroadcastByKey("world_downloader.request", PlayerHelper.getPlayerName(player), file.length() / BYTE_TO_MEGABYTE);

        httpServer.createContext(path, new FileDownloadHandler(file, config.model().bytes_per_second_limit));
        TextHelper.sendTextByKey(player, "world_downloader.response", url);
        return CommandHelper.Return.SUCCESS;
    }

    public static @NotNull File compressRegionFile(@NotNull ServerPlayer player) {
        /* Get region location. */
        ChunkPos chunkPos = player.chunkPosition();
        int regionX = chunkPos.getRegionX();
        int regionZ = chunkPos.getRegionZ();

        /* Get world folder. */
        ServerLevel world = EntityHelper.getServerWorld(player);
        MinecraftServer server = world.getServer();
        ResourceKey<Level> dimensionKey = world.dimension();
        LevelStorageSource.LevelStorageAccess session = server.storageSource;
        File worldDirectory = session.getDimensionPath(dimensionKey).toFile();

        /* Compress files. */
        String regionName = "r." + regionX + "." + regionZ + ".mca";
        List<File> input = new ArrayList<>() {
            {
                this.add(new File(worldDirectory, "region" + File.separator + regionName));
                this.add(new File(worldDirectory, "poi" + File.separator + regionName));
                this.add(new File(worldDirectory, "entities" + File.separator + regionName));
            }
        };
        File output;
        try {
            output = Files.createTempFile(regionName + "#", ".zip").toFile();
            IOUtil.Compressor.compressFiles(worldDirectory, input, output);
        } catch (IOException e) {
            throw ExceptionUtil.makeReThrownException(e);
        }
        LogUtil.info("Generate region file: {}", output.getAbsolutePath());
        return output;
    }

    @EventConsumer
    private static void onServerStarted(@Unused ServerStartedEvent event) {
        downloadContextQueue = EvictingQueue.create(config.model().max_simultaneous_download_count);
    }

    @Override
    protected void onReload() {
        restartHttpServer();
    }

}
