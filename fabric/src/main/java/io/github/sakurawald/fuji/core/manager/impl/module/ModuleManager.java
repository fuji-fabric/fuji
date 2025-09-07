package io.github.sakurawald.fuji.core.manager.impl.module;

import io.github.sakurawald.fuji.core.auxiliary.ExceptionUtil;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.manager.abst.BaseManager;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.service.MixinService;

@Getter
public class ModuleManager extends BaseManager {

    public static final Map<Class<? extends ModuleInitializer>, ModuleInitializer> MODULE_INITIALIZER_BY_CLASS = new HashMap<>();
    public static final Map<String, Class<? extends ModuleInitializer>> MODULE_INITIALIZER_CLASS_BY_MODULE_PATH_STRING = new HashMap<>();

    @Override
    public void onInitialize() {
        invokeModuleInitializers();
    }

    @SuppressWarnings("unchecked")
    private void invokeModuleInitializers() {
        ReflectionUtil.CompileTimeGraph.getCompileTimeTxtGraph(ReflectionUtil.CompileTimeGraph.MODULE_INITIALIZER_GRAPH_FILE_NAME)
            .forEach(className -> {
                try {
                    /* Track the module initializer class. */
                    Class<? extends ModuleInitializer> clazz = (Class<? extends ModuleInitializer>) MixinService.getService().getClassProvider().findClass(className, false);
                    String modulePathString = ModulePathResolver.computeModulePathString(className);
                    ModuleManager.MODULE_INITIALIZER_CLASS_BY_MODULE_PATH_STRING.put(modulePathString, clazz);

                    /* Initialize the module initializer. */
                    boolean enable = ModuleLoadDeterminer.shouldLoadThis(className);
                    if (!enable) return;
                    this.initializeModuleInitializer(clazz);
                } catch (Exception e) {
                    throw ExceptionUtil.makeReThrownException(e);
                }
            });
    }

    public <T extends ModuleInitializer> void initializeModuleInitializer(@NotNull Class<T> clazz) {
        if (!MODULE_INITIALIZER_BY_CLASS.containsKey(clazz)) {
            String className = clazz.getName();
            if (ModuleLoadDeterminer.shouldLoadThis(className)) {
                try {
                    ModuleInitializer moduleInitializer = clazz.getDeclaredConstructor().newInstance();
                    moduleInitializer.doInitialize();
                    MODULE_INITIALIZER_BY_CLASS.put(clazz, moduleInitializer);
                } catch (Exception e) {
                    String modulePath = ModulePathResolver.computeModulePathString(className);
                    LogUtil.error("""


                        [Fuji Module Initialization Failed]
                        ◉ What happened?
                        Unfortunately, the module `{}` could not be initialized.
                        To prevent potential data loss or further issues, the server will now shut down.

                        ◉ Which module?
                        It's `{}` module.

                        ◉ What can I do?
                        1. Verify that there are no `JSON syntax errors` in the module's configuration files.
                        2. Check if any `other mods` are conflicting with `{}` module.
                        3. If you do not require the `{}` module, you may disable it in the `config/fuji/config.json` file, then restart your server.
                        4. If the issue persists, please open an issue at: https://github.com/sakurawald/fuji/issues

                        """, modulePath, modulePath, modulePath, modulePath);
                    throw ExceptionUtil.makeReThrownException(e);
                }
            }
        }
    }

    public void reloadModuleInitializers() {
        MODULE_INITIALIZER_BY_CLASS
            .values()
            .forEach(ModuleManager::reloadModuleInitializer
            );
    }

    private static void reloadModuleInitializer(@NotNull ModuleInitializer initializer) {
        try {
            initializer.doReload();
        } catch (Exception originalException) {
            String modulePath = ModulePathResolver.computeModulePathString(initializer.getClass().getName());
            LogUtil.error("Failed to re-load the module '{}'.", modulePath);
            // NOTE: Throw the original exception to surrounding exception handler.
            throw ExceptionUtil.makeReThrownException(originalException);
        }
    }

}
