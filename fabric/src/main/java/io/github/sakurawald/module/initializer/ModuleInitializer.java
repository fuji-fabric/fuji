package io.github.sakurawald.module.initializer;


import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import lombok.SneakyThrows;

import java.lang.reflect.Field;

/**
 * Tips:
 * 1. Don't catch and handle the command exception, just use @SneakThrow and CommandSyntaxException.
 * 2. Use CommandHelper.Return to provide useful return value.
 * 3. If you use source.sendFeedback() method, then it will be controlled by game rule `sendCommandFeedback`
 * 4. If possible, don't register new ArgumentType, just use the existed ArgumentType. (Mojang provides many useful argument types which implements ArgumentType)
 */
public class ModuleInitializer {

    /**
     * The template-method
     */
    public final void doInitialize() {
        this.registerGsonTypeAdapter();
        this.loadConfigurationFiles();
        this.onInitialize();
        this.registerPlaceholder();
    }

    public final void doReload() {
        this.loadConfigurationFiles();
        this.onReload();
    }

    protected void registerPlaceholder() {
        // no-op
    }

    protected void registerGsonTypeAdapter() {
        // no-op
    }

    @SuppressWarnings("rawtypes")
    @SneakyThrows(IllegalAccessException.class)
    private void loadConfigurationFiles() {
        Field[] declaredFields = this.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            if (declaredField.getType().isAssignableFrom(ObjectConfigurationHandler.class)) {
                ObjectConfigurationHandler configHandler = (ObjectConfigurationHandler) declaredField.get(this);
                LogUtil.debug("invoke readStorage() for field `{}` in class `{}`", declaredField.getName(), this.getClass().getName());
                configHandler.readStorage();
            }
        }
    }

    protected void onInitialize() {
        // no-op
    }

    protected void onReload() {
        // no-op
    }

}
