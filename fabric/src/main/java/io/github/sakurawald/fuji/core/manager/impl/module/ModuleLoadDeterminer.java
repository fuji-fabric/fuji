package io.github.sakurawald.fuji.core.manager.impl.module;


import com.google.gson.JsonObject;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.config.Configs;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.event.inject.StaticEventConsumerInjector;
import io.github.sakurawald.fuji.core.event.inject.structure.EventConsumerInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

public class ModuleLoadDeterminer {

    public static final String ENABLE_JSON_KEY = "enable";
    private static final String MODULES_JSON_KEY = "modules";

    @ForDeveloper("This value is ")
    public static final Map<List<String>, Boolean> MODULE_ENABLE_STATUS = new HashMap<>();

    public static @NotNull List<String> getEnabledModulePaths() {
        List<String> enabledModulePathStrings = new ArrayList<>();
        MODULE_ENABLE_STATUS.forEach((module, enable) -> {
            if (enable) {
                enabledModulePathStrings.add(ModulePathResolver.toModulePathString(module));
            }
        });

        enabledModulePathStrings.sort(String::compareTo);
        return enabledModulePathStrings;
    }

    @ForDeveloper("""
        This method is used to determinate whether to load the given class or not.
        The given class name can be any class name.
        See details in ModulePathResolver.
        """)
    public static boolean shouldLoadThis(@NotNull String className) {
        if (StaticEventConsumerInjector.getEventProducerMixinClassNames().contains(className)) {
            return shouldLoadOnDemandEventMixin(className);
        }

        List<String> modulePathList = ModulePathResolver.toModulePathList(ModulePathResolver.computeModulePathString(className));
        return shouldLoadModulePathList(modulePathList);
    }

    private static boolean shouldLoadOnDemandEventMixin(@NotNull String mixinClassName) {
        /* Apply the event mixin, if there is any event consumer requires it.*/
        List<EventConsumerInfo> validConsumers = ReflectionUtil.CompileTimeGraph
            .getEventGraph()
            .resolveConsumers(mixinClassName)
            .stream()
            .filter(eventConsumerInfo -> shouldLoadThis(eventConsumerInfo.getDeclaringClassName()))
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
    private static boolean shouldLoadModulePathList(@NotNull List<String> modulePathList) {
        if (Configs.MAIN_CONTROL_CONFIG.model().core.debug.disable_all_modules) return false;
        if (modulePathList.get(0).equals(ModulePathResolver.CORE_MODULE_PATH_STRING)) return true;

        /* Use cached value if cache hit. */
        if (MODULE_ENABLE_STATUS.containsKey(modulePathList)) {
            return MODULE_ENABLE_STATUS.get(modulePathList);
        }

        // check enable-supplier
        boolean enable = true;
        JsonObject parent = Configs.MAIN_CONTROL_CONFIG
            .getModelAsJsonTree()
            .get(MODULES_JSON_KEY)
            .getAsJsonObject();

        for (String jsonKey : modulePathList) {
            /* Walk the path. */
            parent = parent.getAsJsonObject(jsonKey);

            /* Check null. */
            if (parent == null || !parent.has(ENABLE_JSON_KEY)) {
                throw new RuntimeException("Missing `enable` json key for module path list `%s`".formatted(modulePathList));
            }

            /* To enable a sub-module, the user should first enable its parent module. */
            if (!parent.getAsJsonPrimitive(ENABLE_JSON_KEY).getAsBoolean()) {
                enable = false;
                break;
            }
        }

        /* To enable a module, the required mods should be installed first. */
        if (!isRequiredModsInstalled(modulePathList)) {
            LogUtil.debug("Refuse to enable module {} (Reason: the required dependency mod for this module isn't installed)", modulePathList);
            enable = false;
        }

        /* Update cache. */
        MODULE_ENABLE_STATUS.put(modulePathList, enable);
        return enable;
    }

    private static boolean isRequiredModsInstalled(@NotNull List<String> modulePath) {
        if (modulePath.contains("carpet")) {
            return FabricLoader.getInstance().isModLoaded("carpet");
        }

        return true;
    }
}
