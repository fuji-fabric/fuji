package mod.fuji.core.service.backup;

import mod.fuji.Fuji;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class RecoveryBackupManager extends BaseBackupManager {

    public RecoveryBackupManager() {
        super(Fuji.MOD_CONFIG_PATH.resolve("backup_recovery"));
    }

    @Override
    protected boolean shouldSkipPath(@NotNull Path dir) {
        return false;
    }

}
