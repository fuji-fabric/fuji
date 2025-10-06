package mod.fuji.core.lifecycle;

import mod.fuji.core.event.EventManager;
import mod.fuji.core.lifecycle.interfaces.ModSubInitializer;
import mod.fuji.core.service.backup.PrimaryBackupManager;
import mod.fuji.core.module.ModuleManager;
import lombok.Getter;

// NOTE: Use lazy evaluation, to resolve the dependency graph easily.
public class ModInitializers {

    @Getter(lazy = true)
    private static final ModSubInitializer moduleManager = new ModuleManager();

    @Getter(lazy = true)
    private static final ModSubInitializer primaryBackupManager = new PrimaryBackupManager();

    @Getter(lazy = true)
    private static final ModSubInitializer eventManager = new EventManager();
}
