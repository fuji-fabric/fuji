package io.github.sakurawald.fuji.module.initializer.world_downloader;

import com.google.common.collect.EvictingQueue;
import com.sun.net.httpserver.HttpServer;
import io.github.sakurawald.fuji.core.auxiliary.RandomUtil;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.IOUtil;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.world_downloader.config.model.WorldDownloaderConfigModel;
import io.github.sakurawald.fuji.module.initializer.world_downloader.structure.FileDownloadHandler;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.level.storage.LevelStorage;
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
    private static final BaseConfigurationHandler<WorldDownloaderConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, WorldDownloaderConfigModel.class);

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
        } catch (IllegalArgumentException ignored) {}
    }

    @Document(id = 1751826617256L, value = "Download the region file around you.")
    @CommandNode("download")
    private static int $download(@CommandSource ServerPlayerEntity player) {
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
        TextHelper.sendBroadcastByKey("world_downloader.request", player.getGameProfile().getName(), file.length() / BYTE_TO_MEGABYTE);

        httpServer.createContext(path, new FileDownloadHandler(file, config.model().bytes_per_second_limit));
        TextHelper.sendTextByKey(player, "world_downloader.response", url);
        return CommandHelper.Return.SUCCESS;
    }

    public static @NotNull File compressRegionFile(@NotNull ServerPlayerEntity player) {
        /* Get region location. */
        ChunkPos chunkPos = player.getChunkPos();
        int regionX = chunkPos.getRegionX();
        int regionZ = chunkPos.getRegionZ();

        /* Get world folder. */
        ServerWorld world = EntityHelper.getServerWorld(player);
        MinecraftServer server = world.getServer();
        RegistryKey<World> dimensionKey = world.getRegistryKey();
        LevelStorage.Session session = server.session;
        File worldDirectory = session.getWorldDirectory(dimensionKey).toFile();

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
            throw new RuntimeException(e);
        }
        LogUtil.info("Generate region file: {}", output.getAbsolutePath());
        return output;
    }

    @Override
    protected void onInitialize() {
        downloadContextQueue = EvictingQueue.create(config.model().max_simultaneous_download_count);
    }

    @Override
    protected void onReload() {
        restartHttpServer();
    }

}
