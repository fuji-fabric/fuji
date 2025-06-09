package tests;

import auxiliary.TestUtility;
import com.google.gson.JsonObject;
import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationParameterValueList;
import io.github.classgraph.ScanResult;
import io.github.sakurawald.Fuji;
import io.github.sakurawald.core.annotation.Cite;
import io.github.sakurawald.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.model.ConfigModel;
import io.github.sakurawald.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GenerateGraphTest {

    public static final Path COMPILE_TIME_SRC_MAIN_RESOURCES_PATH = Path.of("src/main/resources/");
    public static final Path COMPILE_TIME_GRAPH_PATH = COMPILE_TIME_SRC_MAIN_RESOURCES_PATH.resolve(ReflectionUtil.class.getPackageName().replace(".", "/"));
    public static final Path COMPILE_TIME_LANGUAGE_PATH = COMPILE_TIME_SRC_MAIN_RESOURCES_PATH.toAbsolutePath().resolve(Fuji.class.getPackageName().replace(".", "/")).resolve("lang");

    @SneakyThrows(IOException.class)
    @Test
    void generateFromSource() {
        // scan source
        try (ScanResult scanResult = TestUtility.makeBaseClassGraph()
            .enableAllInfo()
            .scan()) {

            Path path = COMPILE_TIME_GRAPH_PATH;
            Files.createDirectories(path);

            try (PrintWriter writer = new PrintWriter(path.resolve(ReflectionUtil.MODULE_INITIALIZER_GRAPH_FILE_NAME).toFile())) {
                scanResult.getSubclasses(ModuleInitializer.class).getNames().stream().sorted().forEach(writer::println);
            }

            try (PrintWriter writer = new PrintWriter(path.resolve(ReflectionUtil.ARGUMENT_TYPE_ADAPTER_GRAPH_FILE_NAME).toFile())) {
                scanResult.getSubclasses(BaseArgumentTypeAdapter.class).getNames().stream().sorted().forEach(writer::println);
            }

            try (PrintWriter writer = new PrintWriter(Path.of("CITE").toFile())) {
                List<String> cites = new ArrayList<>();
                scanResult.getClassesWithAnnotation(Cite.class).forEach(clazz -> {
                    AnnotationInfo annotationInfo = clazz.getAnnotationInfo(Cite.class);
                    AnnotationParameterValueList parameterValues = annotationInfo.getParameterValues();
                    String[] value = (String[]) parameterValues.get("value").getValue();
                    cites.addAll(Arrays.asList(value));
                });
                cites.sort(String::compareTo);
                cites.forEach(writer::println);
            }
        }
    }

    @SneakyThrows(IOException.class)
    @Test
    void generateFromResource() {
        try (PrintWriter writer = new PrintWriter(COMPILE_TIME_GRAPH_PATH.resolve(ReflectionUtil.LANGUAGE_GRAPH_FILE_NAME).toFile())) {
            Arrays.stream(Objects.requireNonNull(COMPILE_TIME_LANGUAGE_PATH.toFile().listFiles())).forEach(file -> writer.println(file.getName()));
        }
    }

    void searchModule(JsonObject parent, String level, List<String> out) {
        // go down
        parent.keySet().stream()
            .filter(key -> parent.get(key).isJsonObject())
            .forEach(key -> searchModule(parent.getAsJsonObject(key), StringUtils.strip(level + "." + key, "."), out));

        // go up
        if (parent.has(ModuleManager.ENABLE_SUPPLIER_KEY)) {
            out.add(level);
        }
    }

    @SneakyThrows(IOException.class)
    @Test
    void generateFromJson() {
        JsonObject modules = BaseConfigurationHandler.getGson().toJsonTree(new ConfigModel())
            .getAsJsonObject().getAsJsonObject("modules");
        ArrayList<String> result = new ArrayList<>();
        searchModule(modules, "", result);
        result.sort(String::compareTo);

        try (PrintWriter writer = new PrintWriter(COMPILE_TIME_GRAPH_PATH.resolve(ReflectionUtil.MODULE_GRAPH_FILE_NAME).toFile())) {
            result.forEach(writer::println);
        }
    }

}
