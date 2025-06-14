package io.github.sakurawald.module.initializer.world_downloader.config.model;

import com.google.gson.annotations.SerializedName;

public class WorldDownloaderConfigModel {

    public String url_format = "http://localhost:%port%%path%";

    public int port = 22222;

    public int bytes_per_second_limit = 128 * 1000;

    @SerializedName(value = "max_simultaneous_download_count", alternate = "context_cache_size")
    public int max_simultaneous_download_count = 5;
}
