package io.github.sakurawald.fuji.core.manager.impl.module;

import com.google.gson.JsonObject;
import io.github.sakurawald.fuji.core.auxiliary.ExceptionUtil;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.config.Configs;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.manager.abst.BaseManager;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.mixin.GlobalMixinConfigPlugin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.service.MixinService;

@Getter
public class ModuleManager extends BaseManager {

    public static final String ENABLE_SUPPLIER_KEY = "enable";
    public static final String CORE_MODULE_PATH = "core";

    private static final Set<String> MODULE_PATHS = new HashSet<>(ReflectionUtil.CompileTimeGraph.getCompileTimeGraph(ReflectionUtil.CompileTimeGraph.MODULE_GRAPH_FILE_NAME));

    public static final Map<List<String>, Boolean> MODULE_ENABLE_STATUS = new HashMap<>();
    private static final Map<String, String> CLASS_NAME_2_MODULE_PATH_STRING = new HashMap<>();
    public static final Map<Class<? extends ModuleInitializer>, ModuleInitializer> MODULE_INITIALIZER_BY_CLASS = new HashMap<>();
    public static final Map<String, Class<? extends ModuleInitializer>> MODULE_INITIALIZER_CLASS_BY_MODULE_PATH_STRING = new HashMap<>();

    public static String computeJoinedModulePath(@NotNull String className) {
        /* This function wrap the computeModulePathAsList function, and providing a cache layer. */
        String modulePathString = CLASS_NAME_2_MODULE_PATH_STRING.get(className);
        if (modulePathString != null) {
            return modulePathString;
        }

        String result = joinModulePath(ModuleManager.computeSplitModulePath(className));
        CLASS_NAME_2_MODULE_PATH_STRING.put(className, result);
        return result;
    }

    public static @NotNull List<String> computeSplitModulePath(@NotNull String className) {
        if (MODULE_PATHS.isEmpty()) {
            LogUtil.warn("This is the first time we generating the module graph file, we just ");
        }

        /* remove leading directories */
        int left = -1;
        List<Class<?>> modulePackagePrefixes = List.of(ModuleInitializer.class, GlobalMixinConfigPlugin.class);
        for (Class<?> modulePackagePrefix : modulePackagePrefixes) {
            String prefix = modulePackagePrefix.getPackageName();
            if (className.startsWith(prefix)) {

                // skip self
                if (className.equals(modulePackagePrefix.getName())) continue;

                left = prefix.length() + 1;
                break;
            }
        }

        if (left == -1) {
            return List.of(CORE_MODULE_PATH);
        }

        String str = className.substring(left);

        /* remove trailing directories */
        int right = str.lastIndexOf(".");
        str = str.substring(0, right);

        List<String> modulePath = new ArrayList<>(List.of(str.split("\\.")));

        if (modulePath.get(0).equals(CORE_MODULE_PATH)) {
            return List.of(CORE_MODULE_PATH);
        }

        /* remove the trailing directories until the string is a module path string */
        String modulePathString = String.join(".", modulePath);
        while (!MODULE_PATHS.contains(modulePathString)) {
            // remove last!
            if (modulePath.isEmpty()) {
                throw new RuntimeException("Can't find the module enable-supplier in `config.json` for class name %s. Did you forget to add the enable-supplier key in ConfigModel ?".formatted(className));
            }
            modulePath.remove(modulePath.size() - 1);

            // compute it
            modulePathString = String.join(".", modulePath);
        }

        return modulePath;
    }

    public static String joinModulePath(List<String> modulePath) {
        return String.join(".", modulePath);
    }

    public static List<String> splitModulePath(String modulePath) {
        return Arrays
            .stream(modulePath.split("\\."))
            .toList();
    }

    public static @NotNull List<String> getEnabledModulePaths() {
        List<String> enabledModuleList = new ArrayList<>();
        MODULE_ENABLE_STATUS.forEach((module, enable) -> {
            if (enable) enabledModuleList.add(joinModulePath(module));
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
        ReflectionUtil.CompileTimeGraph.getCompileTimeGraph(ReflectionUtil.CompileTimeGraph.MODULE_INITIALIZER_GRAPH_FILE_NAME)
            .forEach(className -> {
                try {
                    /* Track the module initializer class. */
                    Class<? extends ModuleInitializer> clazz = (Class<? extends ModuleInitializer>) MixinService.getService().getClassProvider().findClass(className, false);
                    String modulePathString = computeJoinedModulePath(className);
                    ModuleManager.MODULE_INITIALIZER_CLASS_BY_MODULE_PATH_STRING.put(modulePathString, clazz);

                    /* Initialize the module initializer. */
                    boolean enable = Managers.getModuleManager().shouldWeLoadThis(className);
                    if (!enable) return;
                    this.initializeModuleInitializer(clazz);
                } catch (Exception e) {
                    ExceptionUtil.reThrowException(e);
                }
            });
    }

    public <T extends ModuleInitializer> void initializeModuleInitializer(@NotNull Class<T> clazz) {
        if (!MODULE_INITIALIZER_BY_CLASS.containsKey(clazz)) {
            String className = clazz.getName();
            if (shouldWeLoadThis(className)) {
                try {
                    ModuleInitializer moduleInitializer = clazz.getDeclaredConstructor().newInstance();
                    moduleInitializer.doInitialize();
                    MODULE_INITIALIZER_BY_CLASS.put(clazz, moduleInitializer);
                } catch (Exception e) {
                    String modulePath = ModuleManager.computeJoinedModulePath(className);
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
                    ExceptionUtil.reThrowException(e);
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
            String modulePath = ModuleManager.computeJoinedModulePath(initializer.getClass().getName());
            LogUtil.error("Failed to re-load the module '{}'.", modulePath);
            // NOTE: Throw the original exception to surrounding exception handler.
            ExceptionUtil.reThrowException(originalException);
        }
    }

    public boolean shouldWeLoadThis(String className) {
        return shouldWeLoadThis(computeSplitModulePath(className));
    }

    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    private boolean shouldWeLoadThis(@NotNull List<String> modulePath) {
        if (Configs.MAIN_CONTROL_CONFIG.model().core.debug.disable_all_modules) return false;
        if (modulePath.get(0).equals(CORE_MODULE_PATH)) return true;

        // cache
        if (MODULE_ENABLE_STATUS.containsKey(modulePath)) {
            return MODULE_ENABLE_STATUS.get(modulePath);
        }

        // check enable-supplier
        boolean enable = true;
        JsonObject parent = Configs.MAIN_CONTROL_CONFIG.getModelAsJsonTree().get("modules").getAsJsonObject();
        for (String node : modulePath) {
            parent = parent.getAsJsonObject(node);

            if (parent == null || !parent.has(ModuleManager.ENABLE_SUPPLIER_KEY)) {
                throw new RuntimeException("missing `enable supplier` key for dir name list `%s`".formatted(modulePath));
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

    private boolean isRequiredModsInstalled(@NotNull List<String> modulePath) {
        if (modulePath.contains("carpet")) {
            return FabricLoader.getInstance().isModLoaded("carpet");
        }

        return true;
    }

}
