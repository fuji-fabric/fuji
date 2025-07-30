package io.github.sakurawald.fuji.core.auxiliary;

import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtil {

    public static class CompileTimeGraph {

        /* Compile-time generated graphs. */
        public static final String MODULE_INITIALIZER_GRAPH_FILE_NAME = "module-initializer-graph.txt";
        public static final String ARGUMENT_TYPE_ADAPTER_GRAPH_FILE_NAME = "argument-type-adapter-graph.txt";
        public static final String LANGUAGE_GRAPH_FILE_NAME = "language-graph.txt";
        public static final String MODULE_GRAPH_FILE_NAME = "module-graph.txt";

        @SneakyThrows(IOException.class)
        public static List<String> getCompileTimeGraph(String graphName) {
            /* Retrieve the resource file from virtual jar file. */
            InputStream virtualInputStream = ReflectionUtil.class.getResourceAsStream(graphName);
            if (virtualInputStream == null) {
                LogUtil.error("Failed to load the graph {} from virtual jar file. Is the jar file damaged?", graphName);
                throw new RuntimeException("Failed to load the graph " + graphName);
            }

            /* Read the bits from the virtual input stream. */
            @Cleanup BufferedReader reader = new BufferedReader(new InputStreamReader(virtualInputStream));
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        }
    }

    public static List<Method> getMethodsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        return Arrays
            .stream(clazz.getDeclaredMethods())
            .filter(it -> it.isAnnotationPresent(annotation))
            .toList();
    }

    public static Path computeModuleConfigPath(Class<?> clazz) {
        String modulePath = ModuleManager.computeJoinedModulePath(clazz.getName());
        return computeModuleConfigPath(modulePath);
    }

    public static Path computeModuleConfigPath(String modulePath) {
        String others = modulePath.replace(".", "/");
        return Fuji.MOD_CONFIG_PATH
            .resolve("modules")
            .resolve(others);
    }

    public static boolean isPrimitiveWrapperType(Type type) {
        return type == Integer.class
            || type == Float.class
            || type == Byte.class
            || type == Double.class
            || type == Long.class
            || type == Character.class
            || type == Boolean.class
            || type == Short.class
            || type == Void.class;
    }

    public static List<String> extractStackTraceElements(Throwable throwable) {
        return Arrays
            .stream(throwable.getStackTrace())
            .map(StackTraceElement::toString)
            .toList();
    }

    public static @NotNull String getSimpleClassName(Class<?> clazz) {
        /* Handle the special case for anonymous class. */
        String simpleClassName = clazz.getSimpleName();
        if (simpleClassName.isBlank()) {
            simpleClassName = "ANONYMOUS-CLASS";
        }
        return simpleClassName;
    }

    public static class Stacktrace {

        public static String findSourceModuleInCurrentStackTrace() {
            return findSourceModuleAsJoinedModulePath(getCurrentStackTraceAsModuleName());
        }

        private static String findSourceModuleAsJoinedModulePath(List<String> joinedModulePathList) {
            /* The most recent module in the stack trace is considered as the source module. */
            // NOTE: The function defined in mixin class will be injected into the target class. We have no clue to find the source module, for the calls to that function.
            String result = "unknown";
            for (String splitModulePath : joinedModulePathList) {
                result = splitModulePath;
                if (!result.equals(ModuleManager.CORE_MODULE_PATH)) {
                    return result;
                }
            }

            return result;
        }

        private static List<String> getCurrentStackTraceAsClassNames() {
            return Arrays
                .stream(Thread.currentThread().getStackTrace())
                .map(StackTraceElement::getClassName)
                .toList();
        }

        private static List<String> getCurrentStackTraceAsModuleName() {
            return getCurrentStackTraceAsClassNames()
                .stream()
                .map(ModuleManager::computeJoinedModulePath)
                .toList();
        }
    }
}
