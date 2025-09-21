package mod.fuji.module.initializer.works;

import mod.fuji.core.config.mapper.GsonMapper;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.works.config.model.WorksConfigModel;
import mod.fuji.module.initializer.works.config.model.WorksDataModel;
import mod.fuji.module.initializer.works.gui.ListWorksGui;
import mod.fuji.module.initializer.works.config.adapter.WorkTypeAdapter;
import mod.fuji.module.initializer.works.structure.work.abst.Work;
import net.minecraft.server.network.ServerPlayerEntity;

@Document(id = 1751825536620L, value = """
    Provides a `bill-board`, for `players` to post and share their works.
    """)
@ColorBox(id = 1751981616732L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ The difference between `non-production work` and `production work`.
    For a `production work`, we provide the `production sample` to count the `hopper` and `minecart-hopper`.
    """)
@ColorBox(id = 1751981677001L, color = ColorBox.ColorBoxTypes.TIP, value = """
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

}

