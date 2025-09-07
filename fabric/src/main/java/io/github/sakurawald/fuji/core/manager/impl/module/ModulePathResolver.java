package io.github.sakurawald.fuji.core.manager.impl.module;

import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
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

    public static final String CORE_MODULE_PATH_STRING = "core";
    private static final List<String> CORE_MODULE_PATH_LIST = List.of(CORE_MODULE_PATH_STRING);
    private static final Set<String> DECLARED_MODULE_PATH_STRINGS = new HashSet<>(ReflectionUtil.CompileTimeGraph
        .getCompileTimeTxtGraph(ReflectionUtil.CompileTimeGraph.MODULE_GRAPH_FILE_NAME));

    public static final List<Class<?>> MODULE_PACKAGE_PREFIXES = List.of(ModuleInitializer.class, GlobalMixinConfigPlugin.class);

    private static final Map<String, String> CLASS_NAME_2_MODULE_PATH_STRING = new HashMap<>();

    public static @NotNull String toModulePathString(@NotNull List<String> modulePathList) {
        return String.join(".", modulePathList);
    }

    public static @NotNull List<String> toModulePathList(@NotNull String modulePathString) {
        return Arrays
            .stream(modulePathString.split("\\."))
            .toList();
    }

    @ForDeveloper("""
        For consistency, the `core` is treated as a `module` that must always be `enabled`.
        The `core` module can include `packages` from `fuji` mod or any other packages. (Like packages from `JDK standard`)

        This method accepts any class name, and returns a sensible module path string. (The given class name can even be non-existent)
        """)
    public static @NotNull String computeModulePathString(@NotNull String className) {
        /* Use low-level get and put functions, a lazy cache map is good to prevent the concurrent modification and recursive modification. */
        String modulePathString = CLASS_NAME_2_MODULE_PATH_STRING.get(className);
        if (modulePathString != null) {
            return modulePathString;
        }

        /* Cache miss, compute the value now. */
        String result = toModulePathString(computeModulePathList(className));
        CLASS_NAME_2_MODULE_PATH_STRING.put(className, result);
        return result;
    }

    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    @ForDeveloper("""
        1. The LogUtil method calls are banned here, to prevent the recursive-call during the cache computing process.
        2. For simplicity, returned `core` module should not contain any sub-module.
        3. This method only returns a declared `module path list` or `[core]`.
        """)
    public static @NotNull List<String> computeModulePathList(@NotNull String className) {
        if (DECLARED_MODULE_PATH_STRINGS.isEmpty()) {
            System.out.println("This is the first time to generate the module path graph file.");
        }

        /* Remove leading directories. */
        int beginIndex = -1;
        for (Class<?> modulePackagePrefix : MODULE_PACKAGE_PREFIXES) {
            String packageNamePrefix = modulePackagePrefix.getPackageName();

            if (className.startsWith(packageNamePrefix)) {
                // Handle the identity case.
                if (className.equals(modulePackagePrefix.getName())) {
                    continue;
                }

                /* Update the begin index. */
                beginIndex = packageNamePrefix.length() + 1;
                break;
            }
        }

        /* If the class is from other packages, then treating it as included in `core` module. */
        if (beginIndex == -1) {
            return CORE_MODULE_PATH_LIST;
        }

        /* Remove trailing directories. */
        String classNameSubstring = className.substring(beginIndex);
        int endIndex = classNameSubstring.lastIndexOf(".");
        classNameSubstring = classNameSubstring.substring(0, endIndex);

        List<String> modulePathList = new ArrayList<>(List.of(classNameSubstring.split("\\.")));

        // For simplicity, returned `core` module should not contain any sub-module.
        if (modulePathList.get(0).equals(CORE_MODULE_PATH_STRING)) {
            return CORE_MODULE_PATH_LIST;
        }

        /* Remove the trailing directories until the string is a declared module path string. */
        String modulePathString = String.join(".", modulePathList);
        while (!DECLARED_MODULE_PATH_STRINGS.contains(modulePathString)) {
            // Remove the last element.
            if (modulePathList.isEmpty()) {
                throw new RuntimeException("There is not declared module path string for class %s, did you forget to define it in the main control file model?".formatted(className));
            }
            modulePathList.remove(modulePathList.size() - 1);

            // Update the module path string.
            modulePathString = toModulePathString(modulePathList);
        }

        /* Return the declared module path list. */
        return modulePathList;
    }
}
