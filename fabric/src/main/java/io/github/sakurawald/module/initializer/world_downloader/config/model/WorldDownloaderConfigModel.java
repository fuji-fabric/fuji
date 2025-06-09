package io.github.sakurawald.module.initializer.world_downloader.config.model;

public class WorldDownloaderConfigModel {
    public String url_format = "http://localhost:%port%%path%";

    public int port = 22222;

    public int bytes_per_second_limit = 128 * 1000;

    public int context_cache_size = 5;
}
