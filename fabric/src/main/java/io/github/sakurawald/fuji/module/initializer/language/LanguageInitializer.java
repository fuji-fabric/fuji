package io.github.sakurawald.fuji.module.initializer.language;

import io.github.sakurawald.fuji.core.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;

@Document("""
    Respect the `client-side` language option, if possible.
    """)
public class LanguageInitializer extends ModuleInitializer {

    @Override
    protected void onReload() {
        TextHelper.clearLoadedLanguageJsons();
    }

}
