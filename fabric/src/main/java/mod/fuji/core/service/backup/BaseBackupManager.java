package mod.fuji.core.service.backup;

import mod.fuji.Fuji;
import mod.fuji.core.auxiliary.ChronosUtil;
import mod.fuji.core.auxiliary.IOUtil;
import java.io.IOException;
import lombok.SneakyThrows;
import mod.fuji.core.manager.abst.ModSubInitializer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseBackupManager implements ModSubInitializer {

    protected final Path BACKUP_STORAGE_PATH;

    public BaseBackupManager(Path BACKUP_STORAGE_PATH) {
        this.BACKUP_STORAGE_PATH = BACKUP_STORAGE_PATH;
    }

    @SneakyThrows(IOException.class)
    @Override
    public void onInitialize() {
        Files.createDirectories(BACKUP_STORAGE_PATH);
        this.createBackup();
    }

    protected abstract boolean shouldSkipPath(@NotNull Path dir);

    @SneakyThrows(IOException.class)
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
        fileName = IOUtil.makeValidWindowsFileName(fileName);
        return this.BACKUP_STORAGE_PATH.resolve(fileName).toFile();
    }

    public void createBackup() {
        IOUtil.Compressor.compressFiles(Fuji.MOD_CONFIG_PATH.toFile(), this.getInputFiles(), this.getOutputFile());
    }
}
