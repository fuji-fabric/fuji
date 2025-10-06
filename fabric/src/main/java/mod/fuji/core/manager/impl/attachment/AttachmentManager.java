package mod.fuji.core.manager.impl.attachment;

import mod.fuji.Fuji;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AttachmentManager {

    public static final Path ATTACHMENT_STORAGE_PATH = Fuji.MOD_CONFIG_PATH.resolve("attachment");

    private static File makeFile(@NotNull String subject, @NotNull String uuid) throws IOException {
        Path path = ATTACHMENT_STORAGE_PATH.resolve(subject).resolve(uuid);
        Files.createDirectories(path.getParent());
        return path.toFile();
    }

    public static boolean existsAttachment(@NotNull String subject, @Nullable String uuid) {
        if (uuid == null) return false;
        return Files.exists(ATTACHMENT_STORAGE_PATH.resolve(subject).resolve(uuid));
    }

    public static void setAttachment(@NotNull String subject, @NotNull String uuid, @NotNull String data) throws IOException {
        File file = AttachmentManager.makeFile(subject, uuid);
        Files.writeString(file.toPath(), data);
    }

    public static String getAttachment(@NotNull String subject, @NotNull String uuid) throws IOException {
        File file = AttachmentManager.makeFile(subject, uuid);
        return FileUtils.readFileToString(file, Charset.defaultCharset());
    }

    public static boolean unsetAttachment(@NotNull String subject, @NotNull String uuid) throws IOException {
        File file = AttachmentManager.makeFile(subject, uuid);
        return file.delete();
    }

    public static List<String> listSubjectIds(@NotNull String subject) {
        try {
            File[] array = ATTACHMENT_STORAGE_PATH.resolve(subject).toFile().listFiles();
            if (array == null) {
                return Collections.emptyList();
            }

            return Arrays
                .stream(array)
                .filter(File::isFile).map(File::getName).collect(Collectors.toList());
        } catch (Exception ignored) {
            return List.of();
        }
    }

    public static List<String> listSubjectNames() {
        try {
            File[] array = ATTACHMENT_STORAGE_PATH.toFile().listFiles();
            if (array == null) {
                return Collections.emptyList();
            }

            return Arrays
                .stream(array)
                .filter(File::isDirectory).map(File::getName).collect(Collectors.toList());
        } catch (Exception ignored) {
            return List.of();
        }
    }

}
