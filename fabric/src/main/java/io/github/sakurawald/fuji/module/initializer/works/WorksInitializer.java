package io.github.sakurawald.fuji.module.initializer.works;

import io.github.sakurawald.fuji.core.config.mapper.GsonMapper;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.event.message.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.works.config.model.WorksConfigModel;
import io.github.sakurawald.fuji.module.initializer.works.config.model.WorksDataModel;
import io.github.sakurawald.fuji.module.initializer.works.gui.ListWorksGui;
import io.github.sakurawald.fuji.module.initializer.works.job.WorksOnScheduleDispatcherJob;
import io.github.sakurawald.fuji.module.initializer.works.config.adapter.WorkTypeAdapter;
import io.github.sakurawald.fuji.module.initializer.works.structure.work.abst.Work;
import net.minecraft.server.network.ServerPlayerEntity;

@Document(id = 1751825536620L, value = """
    Provides a `bill-board`, for `players` to post and share their works.
    """)
@ColorBox(id = 1751981616732L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ The difference between `non-production work` and `production work`.
    For a `production work`, we provide the `production sample` to count the `hopper` and `minecart-hopper`.
    """)
@ColorBox(id = 1751981677001L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ What is the `sample` in a `production work`?
    It's a counter for `hopper` and `minecart-hopper`.
    It will counts the `rate of items transferred` in defined sample duration.
    For example, you can use it to count how many `bone`, `string` and `coal` is transferred during the sample duration.

    It's something like the `hopper counter` in `carpet` mod.
    You can use both of them at the same time.

    The `hopper counter` provided by `carpet` mod will destroy the output item.
    But the hopper counter provided by this module will not.
    """)
public class WorksInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<WorksDataModel> works = ObjectConfigurationHandler
        .ofModule("works.json", WorksDataModel.class)
        .enableAutoSaveFeature();

    public static final BaseConfigurationHandler<WorksConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, WorksConfigModel.class);

    @Document(id = 1751825541296L, value = "Open the works GUI.")
    @CommandNode("works")
    private static int $works(@CommandSource ServerPlayerEntity player) {
        new ListWorksGui(player, works.model().works, 0)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Override
    protected void registerGsonTypeAdapters() {
        GsonMapper.registerGsonTypeAdapter(Work.class, new WorkTypeAdapter());
    }

    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            WorksOnScheduleDispatcherJob job = WorksOnScheduleDispatcherJob.makeInstance();
            Managers.getScheduleManager().scheduleJob(job);
        });
    }

}

