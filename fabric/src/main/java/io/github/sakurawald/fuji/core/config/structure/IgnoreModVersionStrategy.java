package io.github.sakurawald.fuji.core.config.structure;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;

public class IgnoreModVersionStrategy implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
        return BaseConfigurationHandler.MOD_VERSION_KEY.equals(fieldAttributes.getName());
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
