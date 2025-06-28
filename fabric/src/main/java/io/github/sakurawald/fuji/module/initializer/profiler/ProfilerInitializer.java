package io.github.sakurawald.fuji.module.initializer.profiler;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.profiler.config.ProfilerConfigModel;
import io.github.sakurawald.fuji.module.initializer.profiler.gui.ProfilerGui;
import net.minecraft.server.network.ServerPlayerEntity;

@Document("""
    To query the server health status.
    Including: os, vm, cpu, disk, ram, tps, mspt and gc.
    """)
public class ProfilerInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<ProfilerConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, ProfilerConfigModel.class);

    @Document("Open the server health status GUI.")
    @CommandNode("profiler")
    private static int $profiler(@CommandSource ServerPlayerEntity player) {
        new ProfilerGui(player)
            .open();

        return CommandHelper.Return.SUCCESS;
    }

}
