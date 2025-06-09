package io.github.sakurawald.core.auxiliary;

import io.github.sakurawald.Fuji;
import io.github.sakurawald.core.manager.impl.module.ModuleManager;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

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

@UtilityClass
public class ReflectionUtil {

    /* graphs */
    public static final String MODULE_INITIALIZER_GRAPH_FILE_NAME = "module-initializer-graph.txt";
    public static final String ARGUMENT_TYPE_ADAPTER_GRAPH_FILE_NAME = "argument-type-adapter-graph.txt";
    public static final String LANGUAGE_GRAPH_FILE_NAME = "language-graph.txt";
    public static final String MODULE_GRAPH_FILE_NAME = "module-graph.txt";

    public static List<Method> getMethodsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        return Arrays.stream(clazz.getDeclaredMethods())
            .filter(it -> it.isAnnotationPresent(annotation))
            .toList();
    }

    @SneakyThrows(IOException.class)
    public static List<String> getGraph(String graphName) {
        InputStream inputStream = ReflectionUtil.class.getResourceAsStream(graphName);

        assert inputStream != null;
        @Cleanup BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }

    public static String joinModulePath(List<String> modulePath) {
        return String.join(".", modulePath);
    }

    public static Path computeModuleConfigPath(Class<?> clazz) {
        String modulePath = joinModulePath(ModuleManager.computeModulePath(clazz.getName()));
        return computeModuleConfigPath(modulePath);
    }

    public static Path computeModuleConfigPath(String modulePath) {
        String others = modulePath.replace(".", "/");
        return Fuji.CONFIG_PATH
            .resolve("modules")
            .resolve(others);
    }

    public static boolean isWrapperType(Type type) {
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
}
