package io.github.sakurawald.fuji.core.auxiliary;

import lombok.SneakyThrows;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class IOUtil {

    public static final String THE_MOST_POPULAR_BROWSER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36";

    @SneakyThrows(IOException.class)
    public static void compressFiles(@NotNull File base, @NotNull List<File> input, @NotNull File output) {
        final int BUFFER_SIZE = 4096;

        try (FileOutputStream fos = new FileOutputStream(output);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (File file : input) {
                if (!file.isFile()) continue;

                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(computeArchiveEntryName(base, file));
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[BUFFER_SIZE];
                    int length;

                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }

                    zos.closeEntry();
                }
            }
        }
    }

    private static @NotNull String computeArchiveEntryName(@NotNull File base, @NotNull File file) {
        return computeRelativePath(base, file);
    }

    @SneakyThrows(IOException.class)
    public static String computeRelativePath(@NotNull File base, @NotNull File file) {
        String baseStr = base.getCanonicalPath();
        String fileStr = file.getCanonicalPath();

        Path basePath = Paths.get(baseStr);
        Path filePath = Paths.get(fileStr);
        Path relativize = basePath.relativize(filePath);
        return relativize.toString();
    }

    public static String computeRelativePathBasedOnGameDir(@NotNull File file) {
        return computeRelativePath(FabricLoader.getInstance().getGameDir().toFile(), file);
    }

    @SneakyThrows(IOException.class)
    public static @NotNull List<Path> listLatestFiles(@NotNull Path path) {
        try (Stream<Path> files = Files.list(path)) {
            return files
                .filter(Files::isRegularFile)
                .sorted((o1, o2) -> {
                    try {
                        FileTime t1 = Files.readAttributes(o1, BasicFileAttributes.class).creationTime();
                        FileTime t2 = Files.readAttributes(o2, BasicFileAttributes.class).creationTime();
                        return t1.compareTo(t2);
                    } catch (IOException e) {
                        // NOTE: You can't throw a checked-exception in functional interface.
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
        }
    }

    private static String requestPost(@NotNull URI uri, @NotNull String param) throws IOException {
        LogUtil.debug("Send a post request: uri = {}, param = {}", uri, param);

        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("User-Agent", THE_MOST_POPULAR_BROWSER_AGENT);
        connection.setDoOutput(true);
        connection.setDoInput(true);

        IOUtils.write(param.getBytes(StandardCharsets.UTF_8), connection.getOutputStream());
        return IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
    }

    private static String requestGet(@NotNull URI uri) throws IOException {
        LogUtil.debug("Send a get request: uri = {}", uri);

        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);

        return IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
    }

    public static String requestGet(@NotNull String uri) throws IOException {
        return requestGet(URI.create(uri));
    }

    public static String requestPost(@NotNull String uri, @NotNull String param) throws IOException {
        return requestPost(URI.create(uri), param);
    }
}
