package mod.fuji.core.manager;

import mod.fuji.core.event.EventManager;
import mod.fuji.core.manager.impl.attachment.AttachmentManager;
import mod.fuji.core.manager.impl.backup.BaseBackupManager;
import mod.fuji.core.manager.impl.backup.RecoveryBackupManager;
import mod.fuji.core.manager.impl.backup.PrimaryBackupManager;
import mod.fuji.core.manager.impl.bossbar.BossBarManager;
import mod.fuji.core.manager.impl.cache.CacheManager;
import mod.fuji.core.manager.impl.callback.CallbackManager;
import mod.fuji.core.manager.impl.module.ModuleManager;
import mod.fuji.core.manager.impl.task.GameTaskManager;
import lombok.Getter;

// NOTE: Use lazy evaluation, to resolve the dependency graph easily.
public class Managers {

    @Getter(lazy = true)
    private static final ModuleManager moduleManager = new ModuleManager();

    @Getter(lazy = true)
    private static final BossBarManager bossBarManager = new BossBarManager();

    @Getter(lazy = true)
    private static final BaseBackupManager primaryBackupManager = new PrimaryBackupManager();

    @Getter(lazy = true)
    private static final BaseBackupManager recoveryBackupManager = new RecoveryBackupManager();

    @Getter(lazy = true)
    private static final AttachmentManager attachmentManager = new AttachmentManager();

    @Getter(lazy = true)
    private static final CallbackManager callbackManager = new CallbackManager();

    @Getter(lazy = true)
    private static final GameTaskManager gameTaskManager = new GameTaskManager();

    @Getter(lazy = true)
    private static final CacheManager cacheManager = new CacheManager();

    @Getter(lazy = true)
    private static final EventManager eventManager = new EventManager();
}
