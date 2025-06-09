package io.github.sakurawald.core.manager.impl.attachment;

import io.github.sakurawald.Fuji;
import io.github.sakurawald.core.manager.abst.BaseManager;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AttachmentManager extends BaseManager {

    private static final Path ATTACHMENT_STORAGE_PATH = Fuji.CONFIG_PATH.resolve("attachment");

    @Override
    public void onInitialize() {
        // no-op
    }

    private File makeFile(String subject, String uuid) throws IOException {
        Path path = ATTACHMENT_STORAGE_PATH.resolve(subject).resolve(uuid);
        Files.createDirectories(path.getParent());
        return path.toFile();
    }

    public boolean existsAttachment(String subject, String uuid) {
        if (uuid == null) return false;
        return Files.exists(ATTACHMENT_STORAGE_PATH.resolve(subject).resolve(uuid));
    }

    public void setAttachment(String subject, String uuid, String data) throws IOException {
        File file = this.makeFile(subject, uuid);
        Files.writeString(file.toPath(), data);
    }

    public String getAttachment(String subject, String uuid) throws IOException {
        File file = this.makeFile(subject, uuid);
        return FileUtils.readFileToString(file, Charset.defaultCharset());
    }

    public boolean unsetAttachment(String subject, String uuid) throws IOException {
        File file = this.makeFile(subject, uuid);
        return file.delete();
    }

    public List<String> listSubjectId(String subject) {
        try {
            File[] array = ATTACHMENT_STORAGE_PATH.resolve(subject).toFile().listFiles();
            if (array == null) {
                return Collections.emptyList();
            }

            return Arrays
                .stream(array)
                .filter(File::isFile).map(File::getName).collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<String> listSubjectName() {
        try {
            File[] array = ATTACHMENT_STORAGE_PATH.toFile().listFiles();
            if (array == null) {
                return Collections.emptyList();
            }

            return Arrays
                .stream(array)
                .filter(File::isDirectory).map(File::getName).collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

}
