package io.github.sakurawald.fuji.module.initializer.works;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.manager.impl.scheduler.ScheduleManager;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.works.config.model.WorksConfigModel;
import io.github.sakurawald.fuji.module.initializer.works.config.model.WorksDataModel;
import io.github.sakurawald.fuji.module.initializer.works.gui.WorksGui;
import io.github.sakurawald.fuji.module.initializer.works.job.WorksScheduleJob;
import io.github.sakurawald.fuji.module.initializer.works.structure.work.abst.Work;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.quartz.JobDataMap;

@Document("""
    Provides a bill-board, for players to post and share their works.
    """)
public class WorksInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<WorksDataModel> works = new ObjectConfigurationHandler<>("works.json", WorksDataModel.class)
        .autoSaveEveryMinute();

    public static final BaseConfigurationHandler<WorksConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, WorksConfigModel.class);

    @Document("Open works GUI.")
    @CommandNode("works")
    private static int $works(@CommandSource ServerPlayerEntity player) {
        new WorksGui(player, works.model().works, 0).open();
        return CommandHelper.Return.SUCCESS;
    }

    @Override
    protected void registerGsonTypeAdapter() {
        BaseConfigurationHandler.registerTypeAdapter(Work.class, new Work.WorkTypeAdapter());
    }

    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> new WorksScheduleJob(new JobDataMap() {
            {
                this.put(MinecraftServer.class.getName(), server);
            }
        }, () -> ScheduleManager.CRON_EVERY_MINUTE).schedule());
    }

}

