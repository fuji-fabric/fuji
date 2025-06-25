package io.github.sakurawald.module.initializer.color.anvil;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.service.style_striper.StyleStriper;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.color.anvil.config.model.ColorAnvilConfigModel;
import net.minecraft.entity.player.PlayerEntity;

@Document("""
    This module allows you to use color tags in `anvil` screen.
    """)
public class ColorAnvilInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<ColorAnvilConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, ColorAnvilConfigModel.class);

    public static String stripeStyleTags(PlayerEntity player, String string) {
         return StyleStriper.stripe(player, "anvil", string);
    }
}
