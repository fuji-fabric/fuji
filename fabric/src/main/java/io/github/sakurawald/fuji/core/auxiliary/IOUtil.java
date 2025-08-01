package io.github.sakurawald.fuji.core.auxiliary;

import lombok.SneakyThrows;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
        File base = FabricLoader.getInstance().getGameDir().toFile();
        return computeRelativePath(base, file);
    }

    public static String makeValidWindowsFileName(@NotNull String fileName) {
        String invalidChars = "[<>:\"/\\|?*]";
        return fileName.replaceAll(invalidChars, "_");
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

    public static class Compressor {

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
    }
}
