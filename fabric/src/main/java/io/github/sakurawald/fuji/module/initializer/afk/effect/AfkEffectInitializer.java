package io.github.sakurawald.fuji.module.initializer.afk.effect;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.afk.effect.config.model.AfkEffectConfigModel;

@Document(id = 1751826206965L, value = """
    This module provides special effects for afk player:
    1. Invulnerable: the afk player is immune to all damage.
    2. Targetable: the afk player can not be targeted by a hostile entity.
    3. Moveable: the afk player can not be moved.
    """)
public class AfkEffectInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<AfkEffectConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON_LITERAL, AfkEffectConfigModel.class);
}
