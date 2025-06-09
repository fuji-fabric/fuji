package io.github.sakurawald.core.manager.impl.backup;

import io.github.sakurawald.Fuji;
import io.github.sakurawald.core.auxiliary.ChronosUtil;
import io.github.sakurawald.core.auxiliary.IOUtil;
import io.github.sakurawald.core.config.Configs;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StandardBackupManager extends BaseBackupManager {

    public StandardBackupManager() {
        super(Fuji.CONFIG_PATH.resolve("backup"));
    }

    protected boolean skipPath(@NotNull Path dir) {
        for (String other : Configs.configHandler.model().core.backup.skip) {
            if (dir.equals(Fuji.CONFIG_PATH.resolve(other))) return true;
        }

        return false;
    }

    @Override
    protected @NotNull List<File> getInputFiles() {
        List<File> files = new ArrayList<>();
        try {
            Files.walkFileTree(Fuji.CONFIG_PATH, new SimpleFileVisitor<>() {

                @Override
                public @NotNull FileVisitResult preVisitDirectory(@NotNull Path dir, BasicFileAttributes attrs) {
                    if (BACKUP_PATH.equals(dir)) return FileVisitResult.SKIP_SUBTREE;
                    if (skipPath(dir)) return FileVisitResult.SKIP_SUBTREE;

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public @NotNull FileVisitResult visitFile(@NotNull Path file, BasicFileAttributes attrs) {
                    files.add(file.toFile());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return files;
    }

    @SneakyThrows(IOException.class)
    protected void trimBackup() {
        List<Path> latestFiles = IOUtil.listLatestFiles(BACKUP_PATH);
        Iterator<Path> iterator = latestFiles.iterator();
        while (iterator.hasNext() && latestFiles.size() > Configs.configHandler.model().core.backup.max_slots - 1) {
            Files.delete(iterator.next());
            iterator.remove();
        }
    }

    @Override
    protected @NotNull File getOutputFile() {
        String fileName = ChronosUtil.getCurrentDate() + ".zip";
        return BACKUP_PATH.resolve(fileName).toFile();
    }


    @Override
    protected void makeBackup() {
        IOUtil.compressFiles(Fuji.CONFIG_PATH.toFile(), getInputFiles(), getOutputFile());
    }

    @SneakyThrows(IOException.class)
    @Override
    public void backup() {
        Files.createDirectories(BACKUP_PATH);
        trimBackup();
        makeBackup();
    }
}
