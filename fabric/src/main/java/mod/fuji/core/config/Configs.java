package mod.fuji.core.config;

import mod.fuji.Fuji;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.config.model.MainControlConfigModel;

public class Configs {

    public static final BaseConfigurationHandler<MainControlConfigModel> MAIN_CONTROL_CONFIG = ObjectConfigurationHandler.ofPath(Fuji.MOD_CONFIG_PATH.resolve(BaseConfigurationHandler.CONFIG_JSON_LITERAL), MainControlConfigModel.class);

}
