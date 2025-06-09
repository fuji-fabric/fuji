package io.github.sakurawald.module.initializer.world_downloader;

import com.google.common.collect.EvictingQueue;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.IOUtil;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.world_downloader.config.model.WorldDownloaderConfigModel;
import io.github.sakurawald.module.initializer.world_downloader.structure.FileDownloadHandler;
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
import java.util.UUID;

public class WorldDownloaderInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<WorldDownloaderConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, WorldDownloaderConfigModel.class);

    private static EvictingQueue<String> contextQueue;
    private static HttpServer server;

    private static void initServer() {
        if (server != null) {
            server.stop(0);
        }

        try {
            server = HttpServer.create(new InetSocketAddress(config.model().port), 0);
            server.start();
        } catch (IOException e) {
            LogUtil.error("failed to start http server: {}", e.getMessage());
        }
    }

    private static void safelyRemoveContext(String path) {
        try {
            server.removeContext(path);
        } catch (IllegalArgumentException e) {
            // do nothing
        }
    }

    public static void safelyRemoveContext(@NotNull HttpContext httpContext) {
        safelyRemoveContext(httpContext.getPath());
    }

    @CommandNode("download")
    @Document("Download the region file around you.")
    private static int $download(@CommandSource ServerPlayerEntity player) {
        /* init server */
        if (server == null) {
            initServer();
        }

        /* remove redundant contexts */
        if (contextQueue.remainingCapacity() == 0) {
            LogUtil.debug("contexts is full, remove the oldest context. {}", contextQueue.peek());
            safelyRemoveContext(contextQueue.poll());
        }

        /* create context */
        String url = config.model().url_format;

        int port = config.model().port;
        url = url.replace("%port%", String.valueOf(port));
        String path = "/world-download/" + UUID.randomUUID();
        url = url.replace("%path%", path);

        contextQueue.add(path);
        File file = compressRegionFile(player);
        double BYTE_TO_MEGABYTE = 1.0 * 1024 * 1024;
        TextHelper.sendBroadcastByKey("world_downloader.request", player.getGameProfile().getName(), file.length() / BYTE_TO_MEGABYTE);
        server.createContext(path, new FileDownloadHandler(file, config.model().bytes_per_second_limit));
        TextHelper.sendMessageByKey(player, "world_downloader.response", url);
        return CommandHelper.Return.SUCCESS;
    }

    public static @NotNull File compressRegionFile(@NotNull ServerPlayerEntity player) {
        /* get region location */
        ChunkPos chunkPos = player.getChunkPos();
        int regionX = chunkPos.getRegionX();
        int regionZ = chunkPos.getRegionZ();

        /* get world folder */
        ServerWorld world = player.getServerWorld();
        MinecraftServer server = world.getServer();
        RegistryKey<World> dimensionKey = world.getRegistryKey();
        LevelStorage.Session session = server.session;
        File worldDirectory = session.getWorldDirectory(dimensionKey).toFile();

        /* compress file */
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
            IOUtil.compressFiles(worldDirectory, input, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LogUtil.info("generate region file: {}", output.getAbsolutePath());
        return output;
    }

    @Override
    protected void onInitialize() {
        contextQueue = EvictingQueue.create(config.model().context_cache_size);
    }

    @Override
    protected void onReload() {
        initServer();
    }

}
