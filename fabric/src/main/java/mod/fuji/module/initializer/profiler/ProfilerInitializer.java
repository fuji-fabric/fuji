package mod.fuji.module.initializer.profiler;

import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.profiler.config.ProfilerConfigModel;
import mod.fuji.module.initializer.profiler.gui.ProfilerGui;
import net.minecraft.server.level.ServerPlayer;

@Document(id = 1751824800643L, value = """
    To query the server health status.
    Including: os, vm, cpu, disk, ram, tps, mspt and gc.
    """)
@ColorBox(id = 1751978840922L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ Install the `spark` mod to display the `TPS`, `MSPT` and `CPU` info.
    You need to install the `spark` mod, to provide the `placeholders`.
    To display `tps`, `mspt` and `cpu` info.
    """)
public class ProfilerInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<ProfilerConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, ProfilerConfigModel.class);

    @Document(id = 1751824806374L, value = "Open the server health status GUI.")
    @CommandNode("profiler")
    private static int $profiler(@CommandSource ServerPlayer player) {
        new ProfilerGui(player)
            .open();

        return CommandHelper.Return.SUCCESS;
    }

}
