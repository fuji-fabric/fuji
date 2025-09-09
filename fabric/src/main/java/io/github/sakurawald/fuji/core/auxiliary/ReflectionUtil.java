package io.github.sakurawald.fuji.core.auxiliary;

import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.config.mapper.GsonMapper;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.event.injector.structure.EventGraph;
import io.github.sakurawald.fuji.core.manager.impl.module.ModulePathResolver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

public class ReflectionUtil {

    public static @NotNull String getEnumValuesCompactString(@NotNull Class<?> enumClass) {
        Object[] enumConstants = enumClass.getEnumConstants();
        return Arrays.stream(enumConstants)
            .map(Object::toString)
            .collect(Collectors.joining(", "));
    }

    public static class CompileTimeGraph {

        /* Compile-time generated graphs. */
        public static final String GRAPH_DIRECTORY_NAME = "graph";
        private static final String GRAPH_CLASSPATH_PREFIX = "/" + GRAPH_DIRECTORY_NAME + "/";
        public static final String MODULE_INITIALIZER_GRAPH_FILE_NAME = "module-initializer-graph.txt";
        public static final String ARGUMENT_TYPE_ADAPTER_GRAPH_FILE_NAME = "argument-type-adapter-graph.txt";
        public static final String EVENT_GRAPH_FILE_NAME = "event-graph.json";
        public static final String LANGUAGE_GRAPH_FILE_NAME = "language-graph.txt";
        public static final String MODULE_GRAPH_FILE_NAME = "module-graph.txt";

        @Getter(lazy = true)
        private static final EventGraph eventGraph = makeEventGraph();

        @SneakyThrows(IOException.class)
        public static List<String> getCompileTimeTxtGraph(@NotNull String graphName) {
            InputStream virtualInputStream = getVirtualJarInputStream(graphName);

            /* Read the bits from the virtual input stream. */
            @Cleanup BufferedReader reader = new BufferedReader(new InputStreamReader(virtualInputStream, StandardCharsets.UTF_8));
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        }

        @SneakyThrows(IOException.class)
        private static <T> T getCompileTimeJsonGraph(@NotNull String graphName, @NotNull Class<T> clazz) {
            InputStream virtualInputStream = getVirtualJarInputStream(graphName);
            @Cleanup BufferedReader reader = new BufferedReader(new InputStreamReader(virtualInputStream, StandardCharsets.UTF_8));

            return GsonMapper.fromJson(reader, clazz);
        }

        @ForDeveloper("Retrieve the resource file from virtual jar file.")
        private static @NotNull InputStream getVirtualJarInputStream(@NotNull String graphName) {
            String graphPath = GRAPH_CLASSPATH_PREFIX + graphName;

            InputStream virtualInputStream = ReflectionUtil.class.getResourceAsStream(graphPath);
            if (virtualInputStream == null) {
                LogUtil.error("Failed to load the graph {} from virtual jar file. Is the jar file damaged?", graphName);
                throw new RuntimeException("Failed to load the graph " + graphName);
            }
            return virtualInputStream;
        }

        private static @NotNull EventGraph makeEventGraph() {
            return getCompileTimeJsonGraph(EVENT_GRAPH_FILE_NAME, EventGraph.class);
        }
    }

    public static List<Method> getMethodsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        return Arrays
            .stream(clazz.getDeclaredMethods())
            .filter(it -> it.isAnnotationPresent(annotation))
            .toList();
    }

    public static Path computeModuleConfigPath(Class<?> clazz) {
        String modulePath = ModulePathResolver.computeModulePathString(clazz.getName());
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
                if (!result.equals(ModulePathResolver.CORE_MODULE_PATH_STRING)) {
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
                .map(ModulePathResolver::computeModulePathString)
                .toList();
        }

        public static StackTraceElement getCallerMethod() {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            // [0] = Thread.getStackTrace
            // [1] = this method (getCallerMethod)
            // [2] = the method that called getCallerMethod
            // [3] = the method that called THAT (the actual target caller)
            return stackTrace[3];
        }
    }

}
