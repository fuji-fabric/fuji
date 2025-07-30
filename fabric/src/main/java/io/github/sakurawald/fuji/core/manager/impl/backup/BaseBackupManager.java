package io.github.sakurawald.fuji.core.manager.impl.backup;

import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import io.github.sakurawald.fuji.core.auxiliary.IOUtil;
import io.github.sakurawald.fuji.core.manager.abst.BaseManager;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseBackupManager extends BaseManager {

    protected final Path BACKUP_STORAGE_PATH;

    public BaseBackupManager(Path BACKUP_STORAGE_PATH) {
        this.BACKUP_STORAGE_PATH = BACKUP_STORAGE_PATH;
    }

    @SneakyThrows
    @Override
    public void onInitialize() {
        Files.createDirectories(BACKUP_STORAGE_PATH);
        this.createBackup();
    }

    protected abstract boolean shouldSkipPath(@NotNull Path dir);

    @SneakyThrows
    protected @NotNull List<File> getInputFiles() {
        List<File> files = new ArrayList<>();
        Files.walkFileTree(Fuji.MOD_CONFIG_PATH, new SimpleFileVisitor<>() {

            @Override
            public @NotNull FileVisitResult preVisitDirectory(@NotNull Path dir, BasicFileAttributes attrs) {
                if (BACKUP_STORAGE_PATH.equals(dir)) return FileVisitResult.SKIP_SUBTREE;
                if (shouldSkipPath(dir)) return FileVisitResult.SKIP_SUBTREE;

                return FileVisitResult.CONTINUE;
            }

            @Override
            public @NotNull FileVisitResult visitFile(@NotNull Path file, BasicFileAttributes attrs) {
                files.add(file.toFile());
                return FileVisitResult.CONTINUE;
            }
        });

        return files;
    }

    protected @NotNull File getOutputFile() {
        String fileName = ChronosUtil.Formatter.getFormattedCurrentDate() + ".zip";
        return this.BACKUP_STORAGE_PATH.resolve(fileName).toFile();
    }

    public void createBackup() {
        IOUtil.Compressor.compressFiles(Fuji.MOD_CONFIG_PATH.toFile(), this.getInputFiles(), this.getOutputFile());
    }
}
