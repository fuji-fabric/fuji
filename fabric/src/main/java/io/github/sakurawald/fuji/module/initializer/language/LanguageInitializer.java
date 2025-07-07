package io.github.sakurawald.fuji.module.initializer.language;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;

@Document(id = 1751826368714L, value = """
    Respect the `client-side` language option, if possible.
    """)
public class LanguageInitializer extends ModuleInitializer {

    @Override
    protected void onReload() {
        TextHelper.Loader.clearLoadedLanguageJsons();
    }

}
