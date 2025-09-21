package mod.fuji.core.structure;

import mod.fuji.core.auxiliary.AsyncUtil;
import mod.fuji.core.auxiliary.LogUtil;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

public abstract class Downloader {

    final URL url;
    final Path destination;

    public Downloader(URL url, Path destination) {
        this.url = url;
        this.destination = destination;
    }

    public void startDownload() {
        AsyncUtil.runAsyncAndHandleExceptions(() -> {
            try {
                LogUtil.info("Start download file from {} to {}.", url, destination);
                FileUtils.copyURLToFile(url, destination.toFile());
                onComplete();
                LogUtil.info("End download file from {} to {}.", url, destination);
            } catch (IOException e) {
                LogUtil.error("Failed to download file from {} to {}", url, destination, e);
            }
        });
    }

    public abstract void onComplete();

}
