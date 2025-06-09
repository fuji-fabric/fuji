package io.github.sakurawald.core.manager;

import io.github.sakurawald.core.manager.impl.attachment.AttachmentManager;
import io.github.sakurawald.core.manager.impl.backup.BaseBackupManager;
import io.github.sakurawald.core.manager.impl.backup.RescueBackupManager;
import io.github.sakurawald.core.manager.impl.backup.StandardBackupManager;
import io.github.sakurawald.core.manager.impl.bossbar.BossBarManager;
import io.github.sakurawald.core.manager.impl.callback.CallbackManager;
import io.github.sakurawald.core.manager.impl.command.CommandManager;
import io.github.sakurawald.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.core.manager.impl.scheduler.ScheduleManager;
import lombok.Getter;

/**
 * To avoid trigger the initialization of static field too early, we should use `lazy` loading.
 */
public class Managers {

    @Getter(lazy = true)
    private static final ModuleManager moduleManager = new ModuleManager();

    @Getter(lazy = true)
    private static final BossBarManager bossBarManager = new BossBarManager();

    @Getter(lazy = true)
    private static final ScheduleManager scheduleManager = new ScheduleManager();

    @Getter(lazy = true)
    private static final BaseBackupManager standardBackupManager = new StandardBackupManager();

    @Getter(lazy = true)
    private static final BaseBackupManager rescueBackupManager = new RescueBackupManager();

    @Getter(lazy = true)
    private static final AttachmentManager attachmentManager = new AttachmentManager();

    @Getter(lazy = true)
    private static final CommandManager commandManager = new CommandManager();

    @Getter(lazy = true)
    private static final CallbackManager callbackManager = new CallbackManager();
}
