package io.github.sakurawald.module.initializer.world_downloader.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.core.annotation.Document;

public class WorldDownloaderConfigModel {

    @Document("""
        The url format used to broadcast.
        """)
    public String url_format = "http://localhost:%port%%path%";

    @Document("""
        The port used for downloader http-service.
        """)
    public int port = 22222;

    @Document("""
        Max download speed for each connection.
        """)
    public int bytes_per_second_limit = 128 * 1000;

    @Document("""
        Max download request saved in the memory.
        """)
    @SerializedName(value = "max_simultaneous_download_count", alternate = "context_cache_size")
    public int max_simultaneous_download_count = 5;
}
