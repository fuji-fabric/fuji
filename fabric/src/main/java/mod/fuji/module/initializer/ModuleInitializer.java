package mod.fuji.module.initializer;


import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import lombok.SneakyThrows;

import java.lang.reflect.Field;

public class ModuleInitializer {

    public final void doInitialize() {
        this.registerGsonTypeAdapters();
        this.loadConfigurationFiles();
        this.registerPlaceholders();
    }

    public final void doReload() {
        this.loadConfigurationFiles();
        this.onReload();
    }

    protected void registerPlaceholders() {
        // no-op
    }

    protected void registerGsonTypeAdapters() {
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
                LogUtil.debug("Invoke readStorage() for field `{}` in class `{}`", declaredField.getName(), this.getClass().getName());
                configHandler.readStorage();
            }
        }
    }

    protected void onReload() {
        // no-op
    }

}
