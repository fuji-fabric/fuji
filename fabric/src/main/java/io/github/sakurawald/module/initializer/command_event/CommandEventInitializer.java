package io.github.sakurawald.module.initializer.command_event;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.command.executor.CommandExecutor;
import io.github.sakurawald.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.command_event.config.model.CommandEventConfigModel;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

@Document("""
    This module allows you to execute commands on specified events.
    """)
public class CommandEventInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CommandEventConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, CommandEventConfigModel.class);

    public static void executeCommandOnEvent(ServerPlayerEntity player, List<String> commands) {
        CommandExecutor.execute(ExtendedCommandSource.asConsole(player.getCommandSource()), commands);
    }

}
