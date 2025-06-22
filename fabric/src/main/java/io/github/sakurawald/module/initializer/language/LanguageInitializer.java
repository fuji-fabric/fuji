package io.github.sakurawald.module.initializer.language;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.module.initializer.ModuleInitializer;

@Document("""
    Respect the `client-side` language option, if possible.
    """)
public class LanguageInitializer extends ModuleInitializer {

    @Override
    protected void onReload() {
        TextHelper.clearLoadedLanguageJsons();
    }

}
