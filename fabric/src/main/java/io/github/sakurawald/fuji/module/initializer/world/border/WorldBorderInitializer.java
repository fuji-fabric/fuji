package io.github.sakurawald.fuji.module.initializer.world.border;

import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.world.border.config.model.WorldBorderConfigModel;

@Document(id = 1752561532728L, value = """
    This module allows you to customize the `per-dimension world border`.
    """)
public class WorldBorderInitializer extends ModuleInitializer {

    public static BaseConfigurationHandler<WorldBorderConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, WorldBorderConfigModel.class);


}
