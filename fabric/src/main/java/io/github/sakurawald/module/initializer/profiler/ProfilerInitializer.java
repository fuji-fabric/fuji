package io.github.sakurawald.module.initializer.profiler;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.profiler.config.ProfilerConfigModel;
import io.github.sakurawald.module.initializer.profiler.gui.ProfilerGui;
import net.minecraft.server.network.ServerPlayerEntity;

public class ProfilerInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<ProfilerConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, ProfilerConfigModel.class);

    @CommandNode("profiler")
    @Document("Open the server health status GUI.")
    private static int $profiler(@CommandSource ServerPlayerEntity player) {
        new ProfilerGui(player)
            .open();

        return CommandHelper.Return.SUCCESS;
    }

}
