package io.github.sakurawald.module.initializer.command_warmup;

import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.command_warmup.config.model.CommandWarmupConfigModel;

public class CommandWarmupInitializer extends ModuleInitializer {
    public static final BaseConfigurationHandler<CommandWarmupConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, CommandWarmupConfigModel.class);

}
