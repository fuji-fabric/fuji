package mod.fuji.module.initializer.world_downloader.config.model;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.document.annotation.Document;

public class WorldDownloaderConfigModel {

    @Document(id = 1751826620065L, value = """
        The url format used to broadcast.
        """)
    public String url_format = "http://localhost:%port%%path%";

    @Document(id = 1751826622724L, value = """
        The port used for downloader http-service.
        """)
    public int port = 22222;

    @Document(id = 1751826624437L, value = """
        Max download speed for each connection.
        """)
    public int bytes_per_second_limit = 128 * 1000;

    @Document(id = 1751826626758L, value = """
        Max download request saved in the memory.
        """)
    @SerializedName(value = "max_simultaneous_download_count", alternate = "context_cache_size")
    public int max_simultaneous_download_count = 5;
}
