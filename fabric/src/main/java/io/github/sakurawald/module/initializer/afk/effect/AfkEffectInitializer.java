package io.github.sakurawald.module.initializer.afk.effect;

import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.afk.effect.config.model.AfkEffectConfigModel;

public class AfkEffectInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<AfkEffectConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, AfkEffectConfigModel.class);
}
