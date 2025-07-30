package io.github.sakurawald.fuji.core.manager.impl.backup;

import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.auxiliary.IOUtil;
import io.github.sakurawald.fuji.core.config.Configs;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

public class PrimaryBackupManager extends BaseBackupManager {

    public PrimaryBackupManager() {
        super(Fuji.MOD_CONFIG_PATH.resolve("backup"));
    }

    @Override
    protected boolean shouldSkipPath(@NotNull Path dir) {
        return Configs.MAIN_CONTROL_CONFIG.model().core.backup.skip
            .stream()
            .anyMatch(other -> dir.equals(Fuji.MOD_CONFIG_PATH.resolve(other)));
    }

    @SneakyThrows(IOException.class)
    private void trimBackup() {
        List<Path> latestFiles = IOUtil.listLatestFiles(BACKUP_STORAGE_PATH);
        Iterator<Path> iterator = latestFiles.iterator();
        int maxBackupSlots = Configs.MAIN_CONTROL_CONFIG.model().core.backup.max_slots - 1;
        while (iterator.hasNext()) {
            if (!(latestFiles.size() > maxBackupSlots)) break;
            Files.delete(iterator.next());
            iterator.remove();
        }
    }

    @Override
    public void createBackup() {
        trimBackup();
        super.createBackup();
    }
}
