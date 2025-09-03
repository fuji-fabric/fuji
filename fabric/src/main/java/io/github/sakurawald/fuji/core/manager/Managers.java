package io.github.sakurawald.fuji.core.manager;

import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.manager.impl.attachment.AttachmentManager;
import io.github.sakurawald.fuji.core.manager.impl.backup.BaseBackupManager;
import io.github.sakurawald.fuji.core.manager.impl.backup.RecoveryBackupManager;
import io.github.sakurawald.fuji.core.manager.impl.backup.PrimaryBackupManager;
import io.github.sakurawald.fuji.core.manager.impl.bossbar.BossBarManager;
import io.github.sakurawald.fuji.core.manager.impl.cache.CacheManager;
import io.github.sakurawald.fuji.core.manager.impl.callback.CallbackManager;
import io.github.sakurawald.fuji.core.manager.impl.command.CommandManager;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.fuji.core.manager.impl.scheduler.ScheduleManager;
import io.github.sakurawald.fuji.core.manager.impl.task.GameTaskManager;
import lombok.Getter;

// NOTE: Use lazy evaluation, to resolve the dependency graph easily.
public class Managers {

    @Getter(lazy = true)
    private static final ModuleManager moduleManager = new ModuleManager();

    @Getter(lazy = true)
    private static final BossBarManager bossBarManager = new BossBarManager();

    @Getter(lazy = true)
    private static final ScheduleManager scheduleManager = new ScheduleManager();

    @Getter(lazy = true)
    private static final BaseBackupManager primaryBackupManager = new PrimaryBackupManager();

    @Getter(lazy = true)
    private static final BaseBackupManager recoveryBackupManager = new RecoveryBackupManager();

    @Getter(lazy = true)
    private static final AttachmentManager attachmentManager = new AttachmentManager();

    @Getter(lazy = true)
    private static final CommandManager commandManager = new CommandManager();

    @Getter(lazy = true)
    private static final CallbackManager callbackManager = new CallbackManager();

    @Getter(lazy = true)
    private static final GameTaskManager gameTaskManager = new GameTaskManager();

    @Getter(lazy = true)
    private static final CacheManager cacheManager = new CacheManager();

    @Getter(lazy = true)
    private static final EventManager eventManager = new EventManager();
}
