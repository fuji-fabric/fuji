package io.github.sakurawald.core.structure;

import io.github.sakurawald.core.auxiliary.LogUtil;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public abstract class Downloader {

    final URL url;
    final Path destination;

    public Downloader(URL url, Path destination) {
        this.url = url;
        this.destination = destination;
    }

    public void start() {
        CompletableFuture.runAsync(() -> {
            try {
                LogUtil.info("start download file from {} to {}.", url, destination);
                FileUtils.copyURLToFile(url, destination.toFile());
                onComplete();
                LogUtil.info("end download file from {} to {}.", url, destination);
            } catch (IOException e) {
                LogUtil.error("failed to download file from {} to {}", url, destination);
            }
        });
    }

    public abstract void onComplete();

}
