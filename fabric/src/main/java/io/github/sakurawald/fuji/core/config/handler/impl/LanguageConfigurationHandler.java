package io.github.sakurawald.fuji.core.config.handler.impl;

import com.google.gson.JsonElement;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.config.exception.FailedToLoadResourceException;
import org.jetbrains.annotations.NotNull;

public class LanguageConfigurationHandler extends ResourceConfigurationHandler {

    public static final String FALLBACK_LANGUAGE_CONFIGURATION_DEFAULT_MODEL_RESOURCE_PATH = "lang/en_US.json";

    public LanguageConfigurationHandler(@NotNull String resourcePath) {
        super(resourcePath);
    }

    @Override
    protected JsonElement getDefaultModel() {
        // NOTE: When `language` module is enabled, a player joined with an un-supported language `aa_BB` the first time, a file `lang/aa_BB.json` will be created.
        try {
            return readJsonTreeFromResource(this.resourcePath);
        } catch (FailedToLoadResourceException e) {
            LogUtil.debug("Failed to make the default configuration model from `{}` resource path. (Fallback to the `{}`)", this.resourcePath, FALLBACK_LANGUAGE_CONFIGURATION_DEFAULT_MODEL_RESOURCE_PATH);
            return readJsonTreeFromResource(FALLBACK_LANGUAGE_CONFIGURATION_DEFAULT_MODEL_RESOURCE_PATH);
        }
    }

}

