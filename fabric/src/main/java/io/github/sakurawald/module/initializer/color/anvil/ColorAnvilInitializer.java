package io.github.sakurawald.module.initializer.color.anvil;

import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.color.anvil.config.model.ColorAnvilConfigModel;

public class ColorAnvilInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<ColorAnvilConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, ColorAnvilConfigModel.class);
}
