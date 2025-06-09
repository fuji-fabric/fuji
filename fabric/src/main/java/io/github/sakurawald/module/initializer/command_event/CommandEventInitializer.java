package io.github.sakurawald.module.initializer.command_event;

import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.command_event.config.model.CommandEventConfigModel;

public class CommandEventInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CommandEventConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, CommandEventConfigModel.class);

}
