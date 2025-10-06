package mod.fuji.core.manager;

import mod.fuji.core.event.EventManager;
import mod.fuji.core.service.backup.BaseBackupManager;
import mod.fuji.core.service.backup.PrimaryBackupManager;
import mod.fuji.core.manager.impl.module.ModuleManager;
import lombok.Getter;

// NOTE: Use lazy evaluation, to resolve the dependency graph easily.
public class Managers {

    @Getter(lazy = true)
    private static final ModuleManager moduleManager = new ModuleManager();

    @Getter(lazy = true)
    private static final BaseBackupManager primaryBackupManager = new PrimaryBackupManager();

    @Getter(lazy = true)
    private static final EventManager eventManager = new EventManager();
}
