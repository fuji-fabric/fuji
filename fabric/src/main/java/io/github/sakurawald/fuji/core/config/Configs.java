package io.github.sakurawald.fuji.core.config;

import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.config.model.MainControlConfigModel;

public class Configs {

    public static final BaseConfigurationHandler<MainControlConfigModel> MAIN_CONTROL_CONFIG = new ObjectConfigurationHandler<>(Fuji.MOD_CONFIG_PATH.resolve(BaseConfigurationHandler.CONFIG_JSON), MainControlConfigModel.class);

}
