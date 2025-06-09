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

    private final Map<Class<? extends ModuleInitializer>, ModuleInitializer> moduleRegistry = new HashMap<>();
    private final Map<List<String>, Boolean> module2enable = new HashMap<>();

    /**
     * @return the module path for given class name, if the class is not inside a module, then a special module path List.of("core") will be returned.
     */
    public static @NotNull List<String> computeModulePath(@NotNull String className) {

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

        if (modulePath.getFirst().equals(CORE_MODULE_ROOT)) {
            return List.of(CORE_MODULE_ROOT);
        }

        /* remove the trailing directories until the string is a module path string */
        String modulePathString = String.join(".", modulePath);
        while (!MODULE_PATHS.contains(modulePathString)) {
            // remove last!
            if (modulePath.isEmpty()) {
                throw new RuntimeException("can't find the module enable-supplier in `config.json` for class name %s. Did you forget to add the enable-supplier key in ConfigModel ?".formatted(className));
            }
            modulePath.removeLast();

            // compute it
            modulePathString = String.join(".", modulePath);
        }

        return modulePath;
    }

    @Override
    public void onInitialize() {
        invokeModuleInitializers();
        ServerLifecycleEvents.SERVER_STARTED.register(server -> this.serverStartupReport());
    }

    @SuppressWarnings("unchecked")
    private void invokeModuleInitializers() {
        ReflectionUtil.getGraph(ReflectionUtil.MODULE_INITIALIZER_GRAPH_FILE_NAME)
            .stream()
            .filter(className -> Managers.getModuleManager().shouldWeEnableThis(className))
            .forEach(className -> {
                try {
                    Class<? extends ModuleInitializer> clazz = (Class<? extends ModuleInitializer>) Class.forName(className);
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
        if (!moduleRegistry.containsKey(clazz)) {
            if (shouldWeEnableThis(clazz.getName())) {
                try {
                    ModuleInitializer moduleInitializer = clazz.getDeclaredConstructor().newInstance();
                    moduleInitializer.doInitialize();
                    moduleRegistry.put(clazz, moduleInitializer);
                } catch (Exception e) {
                    LogUtil.error("failed to invoke doInitialize() of module initializer of module {}", clazz.getSimpleName(), e);
                }
            }
        }
    }

    public void reloadModuleInitializers() {
        moduleRegistry.values().forEach(initializer -> {
                try {
                    initializer.doReload();
                } catch (Exception e) {
                    LogUtil.error("failed to reload module.", e);
                }
            }
        );
    }

    private void serverStartupReport() {
        /* report enabled/disabled modules */
        List<String> enabledModuleList = new ArrayList<>();
        module2enable.forEach((module, enable) -> {
            if (enable) enabledModuleList.add(ReflectionUtil.joinModulePath(module));
        });

        enabledModuleList.sort(String::compareTo);
        LogUtil.info("enabled {}/{} modules -> {}", enabledModuleList.size(), module2enable.size(), enabledModuleList);

        /* print first-time helper */
        if (enabledModuleList.size() == 1) {
            firstTimeHelper();
        }
    }

    private void firstTimeHelper() {
        LogUtil.info("""

            [Fuji User Guide]
            It seems that this is the first time you use fuji mod.

            Here are some important points:
            - Fuji is designed to be fully-modular, that is to say, all modules are disabled by default.
            - To enable a module, is just like ordering food at a restaurant: you can enable a module by modifying the `config/fuji/config.json` file, and re-start the server to apply the modification.
                - To use `/tpa` command, enable the `tpa` module.
                - To use placeholders provided by fuji, enable the `placeholder` module.
                - To use echo commands like `/send-message`, `/send-broadcast` etc, enable the `echo` module.
            - To see the list of modules, and what functionality they provides, read the `fuji manual` pdf file in https://github.com/sakurawald/fuji/raw/dev/docs/release/fuji.pdf
            - Anything unclear, open an issue in https://github.com/sakurawald/fuji/issues
            """);
    }

    public boolean shouldWeEnableThis(String className) {
        return shouldWeEnableThis(computeModulePath(className));
    }

    private boolean shouldWeEnableThis(@NotNull List<String> modulePath) {
        if (Configs.configHandler.model().core.debug.disable_all_modules) return false;
        if (modulePath.getFirst().equals(CORE_MODULE_ROOT)) return true;

        // cache
        if (module2enable.containsKey(modulePath)) {
            return module2enable.get(modulePath);
        }

        // check enable-supplier
        boolean enable = true;
        JsonObject parent = Configs.configHandler.convertModelToJsonTree().getAsJsonObject().get("modules").getAsJsonObject();
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
            LogUtil.debug("refuse to enable module {} (reason: the required dependency mod for this module isn't installed, please read the official wiki!)", modulePath);
            enable = false;
        }

        // cache
        module2enable.put(modulePath, enable);
        return enable;
    }

    private boolean isRequiredModsInstalled(@NotNull List<String> modulePath) {

        if (modulePath.contains("carpet")) {
            return FabricLoader.getInstance().isModLoaded("carpet");
        }

        return true;
    }
}
