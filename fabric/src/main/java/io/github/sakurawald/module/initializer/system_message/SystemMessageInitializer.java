package io.github.sakurawald.module.initializer.system_message;

import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.system_message.config.model.SystemMessageConfigModel;

public class SystemMessageInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<SystemMessageConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, SystemMessageConfigModel.class);

}
