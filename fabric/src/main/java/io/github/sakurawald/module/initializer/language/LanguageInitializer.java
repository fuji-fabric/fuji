package io.github.sakurawald.module.initializer.language;

import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.module.initializer.ModuleInitializer;


public class LanguageInitializer extends ModuleInitializer {

    @Override
    protected void onReload() {
        TextHelper.clearLoadedLanguageJsons();
    }

}
