package io.github.sakurawald.core.config;

import io.github.sakurawald.Fuji;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.config.model.ConfigModel;

public class Configs {

    public static final BaseConfigurationHandler<ConfigModel> configHandler = new ObjectConfigurationHandler<>(Fuji.CONFIG_PATH.resolve(BaseConfigurationHandler.CONFIG_JSON), ConfigModel.class);

}
