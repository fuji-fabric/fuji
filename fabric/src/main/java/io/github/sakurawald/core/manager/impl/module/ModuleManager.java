package io.github.sakurawald.core.manager.impl.module;

import com.google.gson.JsonObject;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.core.config.Configs;
import io.github.sakurawald.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.core.manager.Managers;
import io.github.sakurawald.core.manager.abst.BaseManager;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.mixin.GlobalMixinConfigPlugin;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public class ModuleManager extends BaseManager {

    public static final String ENABLE_SUPPLIER_KEY = "enable";
    public static final String CORE_MODULE_ROOT = "core";

    private static final Set<String> MODULE_PATHS = new HashSet<>(ReflectionUtil.getGraph(ReflectionUtil.MODULE_GRAPH_FILE_NAME));

    public static final Map<List<String>, Boolean> MODULE_ENABLE_STATUS = new HashMap<>();
    private static final Map<String, String> CLASS_NAME_2_MODULE_PATH_STRING = new HashMap<>();
    public static final Map<Class<? extends ModuleInitializer>, ModuleInitializer> MODULE_INITIALIZER_BY_CLASS = new HashMap<>();
    public static final Map<String, Class<? extends ModuleInitializer>> MODULE_INITIALIZER_CLASS_BY_MODULE_PATH_STRING = new HashMap<>();


    public static String computeModulePathAsString(@NotNull String className) {
        /* This function wrap the computeModulePathAsList function, and providing a cache layer. */
        String modulePathString = CLASS_NAME_2_MODULE_PATH_STRING.get(className);
        if (modulePathString != null) {
            return modulePathString;
        }

        String result = joinModulePath(ModuleManager.computeModulePathAsList(className));
        CLASS_NAME_2_MODULE_PATH_STRING.put(className, result);
        return result;
    }

    /**
     * @return the module path for given class name, if the class is not inside a module, then a special module path List.of("core") will be returned.
     */
    public static @NotNull List<String> computeModulePathAsList(@NotNull String className) {
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
            return List.of(CORE_MODULE_ROOT);
        }

        String str = className.substring(left);

        /* remove trailing directories */
        int right = str.lastIndexOf(".");
        str = str.substring(0, right);

        List<String> modulePath = new ArrayList<>(List.of(str.split("\\.")));

        if (modulePath.get(0).equals(CORE_MODULE_ROOT)) {
            return List.of(CORE_MODULE_ROOT);
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

    @Override
    public void onInitialize() {
        invokeModuleInitializers();
        ServerLifecycleEvents.SERVER_STARTED.register(server -> this.serverStartupReport());
    }

    @SuppressWarnings("unchecked")
    private void invokeModuleInitializers() {
        ReflectionUtil.getGraph(ReflectionUtil.MODULE_INITIALIZER_GRAPH_FILE_NAME)
            .forEach(className -> {
                try {
                    /* Track the module initializer class. */
                    Class<? extends ModuleInitializer> clazz = (Class<? extends ModuleInitializer>) Class.forName(className);
                    String modulePathString = computeModulePathAsString(className);
                    ModuleManager.MODULE_INITIALIZER_CLASS_BY_MODULE_PATH_STRING.put(modulePathString, clazz);

                    /* Initialize the module initializer. */
                    boolean enable = Managers.getModuleManager().shouldWeLoadThis(className);
                    if (!enable) return;
                    this.initializeModuleInitializer(clazz);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * If a module is enabled, but the module doesn't extend AbstractModule, then this method will also return null, but the module doesn't extend AbstractModule, then this method will also return null.
     */
    public <T extends ModuleInitializer> void initializeModuleInitializer(@NotNull Class<T> clazz) {
        if (!MODULE_INITIALIZER_BY_CLASS.containsKey(clazz)) {
            String className = clazz.getName();
            if (shouldWeLoadThis(className)) {
                try {
                    ModuleInitializer moduleInitializer = clazz.getDeclaredConstructor().newInstance();
                    moduleInitializer.doInitialize();
                    MODULE_INITIALIZER_BY_CLASS.put(clazz, moduleInitializer);
                } catch (Exception e) {
                    LogUtil.error("Failed to invoke doInitialize() of module initializer of module {}", clazz.getSimpleName(), e);
                }
            }
        }
    }

    public void reloadModuleInitializers() {
        MODULE_INITIALIZER_BY_CLASS.values().forEach(initializer -> {
                try {
                    initializer.doReload();
                } catch (Exception e) {
                    LogUtil.error("Failed to reload module.", e);
                }
            }
        );
    }

    private void serverStartupReport() {
        /* report enabled/disabled modules */
        List<String> enabledModuleList = new ArrayList<>();
        MODULE_ENABLE_STATUS.forEach((module, enable) -> {
            if (enable) enabledModuleList.add(joinModulePath(module));
        });

        enabledModuleList.sort(String::compareTo);
        LogUtil.info("Enabled {}/{} modules -> {}", enabledModuleList.size(), MODULE_ENABLE_STATUS.size(), enabledModuleList);

        /* print first-time helper */
        if (enabledModuleList.size() == 1 || FabricLoader.getInstance().isDevelopmentEnvironment()) {
            printUserGuide();
        }
    }

    public static void printUserGuide() {
        // NOTE: The generator is https://rebane2001.com/discord-colored-text-generator/
        String userGuide = """
            [2;35m[1;35m
            [Fuji User Guide][0m[2;35m
            It seems that this is the first time you use fuji mod.

            Here are some important points:
            - Fuji is designed to be fully-modular, that is to say, [2;34mall modules are disabled by default.[0m[2;35m
            - To enable a module: modify the `[2;34mconfig/fuji/config.json[0m[2;35m` file, and [2;34mre-start[0m[2;35m the server to apply the modification.
                - To use `/tpa` command, enable the `tpa` module.
                - To use placeholders provided by fuji, enable the `placeholder` module.
                - To use echo commands like `/send-message`, `/send-broadcast` etc, enable the `echo` module.
            - To see the list of modules, and what functionality they provides, read the `fuji manual` pdf file in [2;34mhttps://github.com/sakurawald/fuji/raw/dev/docs/release/fuji.pdf[0m[2;35m
            - To discover new things, use `/fuji inspect` command.
            - Anything unclear, open an issue in [2;34mhttps://github.com/sakurawald/fuji/issues[0m[2;35m[0m
           """;
        LogUtil.info(userGuide);
    }

    public boolean shouldWeLoadThis(String className) {
        return shouldWeLoadThis(computeModulePathAsList(className));
    }

    private boolean shouldWeLoadThis(@NotNull List<String> modulePath) {
        if (Configs.mainControlConfig.model().core.debug.disable_all_modules) return false;
        if (modulePath.get(0).equals(CORE_MODULE_ROOT)) return true;

        // cache
        if (MODULE_ENABLE_STATUS.containsKey(modulePath)) {
            return MODULE_ENABLE_STATUS.get(modulePath);
        }

        // check enable-supplier
        boolean enable = true;
        JsonObject parent = Configs.mainControlConfig.convertModelToJsonTree().getAsJsonObject().get("modules").getAsJsonObject();
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
