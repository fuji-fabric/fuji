package io.github.sakurawald.fuji.core.manager.impl.module;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.mixin.GlobalMixinConfigPlugin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class ModulePathResolver {

    public static final String CORE_MODULE_PATH = "core";

    private static final Set<String> DECLARED_MODULE_PATHS = new HashSet<>(ReflectionUtil.CompileTimeGraph
        .getCompileTimeTxtGraph(ReflectionUtil.CompileTimeGraph.MODULE_GRAPH_FILE_NAME));

    private static final Map<String, String> CLASS_NAME_2_MODULE_PATH_STRING = new HashMap<>();

    public static @NotNull String toModulePathString(@NotNull List<String> modulePath) {
        return String.join(".", modulePath);
    }

    public static @NotNull List<String> toModulePathList(@NotNull String modulePath) {
        return Arrays
            .stream(modulePath.split("\\."))
            .toList();
    }

    public static @NotNull String computeModulePathString(@NotNull String className) {
        /* This function wrap the computeModulePathAsList function, and providing a cache layer. */
        String modulePathString = CLASS_NAME_2_MODULE_PATH_STRING.get(className);
        if (modulePathString != null) {
            return modulePathString;
        }

        String result = toModulePathString(computeModulePathList(className));
        CLASS_NAME_2_MODULE_PATH_STRING.put(className, result);
        return result;
    }

    public static @NotNull List<String> computeModulePathList(@NotNull String className) {
        if (DECLARED_MODULE_PATHS.isEmpty()) {
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
        while (!DECLARED_MODULE_PATHS.contains(modulePathString)) {
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
}
