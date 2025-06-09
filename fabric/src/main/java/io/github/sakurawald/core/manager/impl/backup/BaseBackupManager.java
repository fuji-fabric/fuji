package io.github.sakurawald.core.manager.impl.backup;

import io.github.sakurawald.core.manager.abst.BaseManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public abstract class BaseBackupManager extends BaseManager {

    protected final Path BACKUP_PATH;

    public BaseBackupManager(Path BACKUP_PATH) {
        this.BACKUP_PATH = BACKUP_PATH;
    }

    @Override
    public void onInitialize() {
        // template method
        this.backup();
    }

    protected abstract @NotNull List<File> getInputFiles();

    protected abstract @NotNull File getOutputFile();

    protected abstract void makeBackup();

    public abstract void backup();
}
