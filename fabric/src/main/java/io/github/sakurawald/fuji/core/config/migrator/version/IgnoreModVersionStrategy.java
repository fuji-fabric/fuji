package io.github.sakurawald.fuji.core.config.migrator.version;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class IgnoreModVersionStrategy implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
        return VersionPropertyInjector.MOD_VERSION_KEY.equals(fieldAttributes.getName());
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
