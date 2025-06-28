package tests;

import auxiliary.TestUtility;
import com.google.gson.JsonObject;
import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationParameterValueList;
import io.github.classgraph.ScanResult;
import io.github.sakurawald.fuji.core.document.annotation.Cite;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.model.ConfigModel;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GenerateGraphTest {

    public static final Path COMPILE_TIME_RESOURCE_PATH = Path.of("../common/src/main/resources/");

    public static final Path COMPILE_TIME_GRAPH_PATH = COMPILE_TIME_RESOURCE_PATH.resolve(ReflectionUtil.class.getPackageName().replace(".", "/"));

    public static final Path COMPILE_TIME_LANGUAGE_PATH = Path.of("../crowdin/pull-from-crowdin/");

    @SneakyThrows(IOException.class)
    @Test
    void generateStuffsFromRuntimeEnvironment() {
        // scan source
        try (ScanResult scanResult = TestUtility.makeBaseClassGraph()
            .enableAllInfo()
            .scan()) {

            Path path = COMPILE_TIME_GRAPH_PATH;
            Files.createDirectories(path);

            /* Generate module-initializer-graph.txt file. */
            File moduleInitializerGraphFile = path.resolve(ReflectionUtil.MODULE_INITIALIZER_GRAPH_FILE_NAME).toFile();
            try (PrintWriter writer = new PrintWriter(moduleInitializerGraphFile)) {
                scanResult.getSubclasses(ModuleInitializer.class).getNames().stream().sorted().forEach(writer::println);
            }

            /* Generate argument-type-adapter-graph.txt file. */
            File argumentAdapterGraphFile = path.resolve(ReflectionUtil.ARGUMENT_TYPE_ADAPTER_GRAPH_FILE_NAME).toFile();
            try (PrintWriter writer = new PrintWriter(argumentAdapterGraphFile)) {
                scanResult.getSubclasses(BaseArgumentTypeAdapter.class).getNames().stream().sorted().forEach(writer::println);
            }

            /* Generate CITE file. */
            try (PrintWriter writer = new PrintWriter(Path.of("../CITE").toFile())) {
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
        /* Generate language-graph.txt file. */
        File languageGraphFile = COMPILE_TIME_GRAPH_PATH.resolve(ReflectionUtil.LANGUAGE_GRAPH_FILE_NAME).toFile();
        try (PrintWriter writer = new PrintWriter(languageGraphFile)) {
            File languageFilesPath = COMPILE_TIME_LANGUAGE_PATH.toFile();
            Arrays.stream(Objects.requireNonNull(languageFilesPath.listFiles()))
                .forEach(file -> writer.println(file.getName()));
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
        /* Generate module-graph.txt file. */
        JsonObject modules = BaseConfigurationHandler.getGson().toJsonTree(new ConfigModel())
            .getAsJsonObject().getAsJsonObject("modules");
        ArrayList<String> result = new ArrayList<>();
        searchModule(modules, "", result);
        result.sort(String::compareTo);

        Path moduleGraphFile = COMPILE_TIME_GRAPH_PATH.resolve(ReflectionUtil.MODULE_GRAPH_FILE_NAME);
        Files.createDirectories(moduleGraphFile.getParent());
        try (PrintWriter writer = new PrintWriter(moduleGraphFile.toFile())) {
            result.forEach(writer::println);
        }
    }

}
