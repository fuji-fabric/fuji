package io.github.sakurawald.module.initializer.command_rewrite;

import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.command_rewrite.config.model.CommandRewriteConfigModel;

public class CommandRewriteInitializer extends ModuleInitializer {
    public static final BaseConfigurationHandler<CommandRewriteConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, CommandRewriteConfigModel.class);
}
