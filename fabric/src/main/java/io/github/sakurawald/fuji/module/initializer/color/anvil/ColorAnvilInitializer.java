package io.github.sakurawald.fuji.module.initializer.color.anvil;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.service.style_striper.StyleStriper;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.color.anvil.config.model.ColorAnvilConfigModel;
import net.minecraft.entity.player.PlayerEntity;

@Document(id = 1751824946979L, value = """
    This module allows you to use color tags in `anvil` screen.
    """)
public class ColorAnvilInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<ColorAnvilConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, ColorAnvilConfigModel.class);

    public static String stripeStyleTags(PlayerEntity player, String string) {
         return StyleStriper.stripe(player, "anvil", string);
    }
}
