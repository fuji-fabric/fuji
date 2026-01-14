package mod.fuji.module.initializer.color;

import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.structure.RegexRewriteRule;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.color.config.model.ColorConfigModel;
import org.jetbrains.annotations.NotNull;


@Document(id = 1766444546546L, value = """
    This module introduces `style tags` in some `places`.
    """)
public class ColorInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<ColorConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, ColorConfigModel.class);

    public static @NotNull String rewriteColorCodes(@NotNull String string) {
        for (RegexRewriteRule rule : config.model().getRewrite().getRules()) {
            string = rule.apply(string);
        }

        return string;
    }

}
