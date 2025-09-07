package io.github.sakurawald.fuji.core.manager.impl.module;

import com.google.gson.JsonObject;
import io.github.sakurawald.fuji.core.auxiliary.ExceptionUtil;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.config.Configs;
import io.github.sakurawald.fuji.core.event.inject.structure.EventConsumerInfo;
import io.github.sakurawald.fuji.core.manager.abst.BaseManager;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.service.MixinService;

@Getter
public class ModuleManager extends BaseManager {

    public static final String ENABLE_SUPPLIER_KEY = "enable";
    private static final String MODULES_LITERAL = "modules";

    public static final Map<List<String>, Boolean> MODULE_ENABLE_STATUS = new HashMap<>();
    public static final Map<Class<? extends ModuleInitializer>, ModuleInitializer> MODULE_INITIALIZER_BY_CLASS = new HashMap<>();
    public static final Map<String, Class<? extends ModuleInitializer>> MODULE_INITIALIZER_CLASS_BY_MODULE_PATH_STRING = new HashMap<>();

    public static @NotNull List<String> getEnabledModulePaths() {
        List<String> enabledModuleList = new ArrayList<>();
        MODULE_ENABLE_STATUS.forEach((module, enable) -> {
            if (enable) enabledModuleList.add(ModulePathResolver.toModulePathString(module));
        });

        enabledModuleList.sort(String::compareTo);
        return enabledModuleList;
    }

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
                    boolean enable = ModuleManager.shouldLoadThis(className);
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
            if (shouldLoadThis(className)) {
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

    public static boolean shouldLoadThis(@NotNull String className) {
        if (className.contains(".on_demand.")) {
            return shouldLoadOnDemandEventMixin(className);
        }

        return shouldLoadModule(ModulePathResolver.computeModulePathList(className));
    }

    private static boolean shouldLoadOnDemandEventMixin(@NotNull String mixinClassName) {
        /* Apply the event mixin, if there is any event consumer requires it.*/
        List<EventConsumerInfo> validConsumers = ReflectionUtil.CompileTimeGraph
            .getEventGraph()
            .resolveConsumers(mixinClassName)
            .stream()
            .filter(eventConsumerInfo -> ModuleManager.shouldLoadThis(eventConsumerInfo.getDeclaringClassName()))
            .toList();

        if (validConsumers.isEmpty()) {
            LogUtil.debug("Skip applying the on-demand event mixin '{}', there are no consumers.", mixinClassName);
            return false;
        } else {
            LogUtil.debug("Apply the on-demand event mixin '{}', to satisfy the consumers: {}", mixinClassName, validConsumers);
            return true;
        }
    }

    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    private static boolean shouldLoadModule(@NotNull List<String> modulePath) {
        if (Configs.MAIN_CONTROL_CONFIG.model().core.debug.disable_all_modules) return false;
        if (modulePath.get(0).equals(ModulePathResolver.CORE_MODULE_PATH_STRING)) return true;

        // cache
        if (MODULE_ENABLE_STATUS.containsKey(modulePath)) {
            return MODULE_ENABLE_STATUS.get(modulePath);
        }

        // check enable-supplier
        boolean enable = true;
        JsonObject parent = Configs.MAIN_CONTROL_CONFIG.getModelAsJsonTree().get(MODULES_LITERAL).getAsJsonObject();
        for (String node : modulePath) {
            parent = parent.getAsJsonObject(node);

            if (parent == null || !parent.has(ModuleManager.ENABLE_SUPPLIER_KEY)) {
                throw new RuntimeException("Missing `enable supplier` key for dir name list `%s`".formatted(modulePath));
            }

            // only enable a sub-module if the parent module is enabled.
            if (!parent.getAsJsonPrimitive(ModuleManager.ENABLE_SUPPLIER_KEY).getAsBoolean()) {
                enable = false;
                break;
            }
        }

        // soft fail if required mod is not installed.
        if (!isRequiredModsInstalled(modulePath)) {
            LogUtil.debug("Refuse to enable module {} (reason: the required dependency mod for this module isn't installed, please read the official wiki!)", modulePath);
            enable = false;
        }

        // cache
        MODULE_ENABLE_STATUS.put(modulePath, enable);
        return enable;
    }

    private static boolean isRequiredModsInstalled(@NotNull List<String> modulePath) {
        if (modulePath.contains("carpet")) {
            return FabricLoader.getInstance().isModLoaded("carpet");
        }

        return true;
    }

}
