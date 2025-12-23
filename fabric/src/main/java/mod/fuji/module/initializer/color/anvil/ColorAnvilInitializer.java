package mod.fuji.module.initializer.color.anvil;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.service.style_striper.StyleStriper;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.color.anvil.config.model.ColorAnvilConfigModel;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

@Document(id = 1751824946979L, value = """
    This module introduces `style tags` in `anvil block`.
    """)
public class ColorAnvilInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<ColorAnvilConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, ColorAnvilConfigModel.class);

    public static @NotNull String stripeStyleTags(@NotNull Player player, @NotNull String string) {
         return StyleStriper.stripe(player, "anvil", string);
    }
}
