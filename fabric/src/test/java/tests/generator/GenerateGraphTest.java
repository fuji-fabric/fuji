package tests.generator;

import auxiliary.classgraph.ClassGraphUtil;
import auxiliary.TestUtil;
import auxiliary.classgraph.structure.ExtendedAnnotationInfo;
import com.google.gson.JsonObject;
import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationParameterValueList;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import mod.fuji.core.auxiliary.ReflectionUtil;
import mod.fuji.core.auxiliary.StringUtil;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.config.mapper.GsonMapper;
import mod.fuji.core.config.model.MainControlConfigModel;
import mod.fuji.core.document.annotation.Cite;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.core.module.ModuleLoadDeterminer;
import mod.fuji.core.module.ModulePathResolver;
import mod.fuji.module.initializer.ModuleInitializer;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class GenerateGraphTest {

    private static final Path COMPILE_TIME_CITE_FILE_PATH = TestUtil.ROOT_PROJECT_ROOT_PATH.resolve("CITE.md");
    private static final Path COMPILE_TIME_TEST_CASE_FILE_PATH = TestUtil.ROOT_PROJECT_ROOT_PATH.resolve("TEST-CASE.md");

    @Test
    @SneakyThrows(IOException.class)
    public void generateBitsFromCodebase() {
        /* Scan the codebase. */
        try (ScanResult scanResult = ClassGraphUtil.makeScanResult()) {

            Files.createDirectories(TestUtil.COMPILE_TIME_GRAPH_PATH);

            /* Generate module-initializer-graph.txt file. */
            generateModuleInitializerGraphFile(scanResult);

            /* Generate argument-type-adapter-graph.txt file. */
            generateArgumentTypeAdapterGraphFile(scanResult);

            /* Generate CITE file. */
            generateCiteFile(scanResult);

            /* Generate TEST-CASE file. */
            generateTestCaseFile(scanResult);
        }
    }

    @SneakyThrows(IOException.class)
    private static void generateCiteFile(ScanResult scanResult) {
        try (PrintWriter writer = new PrintWriter(COMPILE_TIME_CITE_FILE_PATH.toFile(), StandardCharsets.UTF_8)) {

            List<ExtendedAnnotationInfo> citeAnnotationList = ClassGraphUtil.findTargetAnnotationInstancesAnywhere(scanResult, Cite.class, false);
            citeAnnotationList
                .stream()
                .map(ExtendedAnnotationInfo::getAnnotationInfo)
                .flatMap(annotationInfo -> {
                    AnnotationParameterValueList parameterValues = annotationInfo.getParameterValues();
                    String[] value = (String[]) parameterValues.get("value").getValue();
                    return Arrays.stream(value);
                })
                .sorted()
                .forEach(it -> writer.println(it + "\n"));
        }
    }

    @SneakyThrows(IOException.class)
    private static void generateTestCaseFile(ScanResult scanResult) {
        try (PrintWriter writer = new PrintWriter(COMPILE_TIME_TEST_CASE_FILE_PATH.toFile(), StandardCharsets.UTF_8)) {

            List<ExtendedAnnotationInfo> annotationList = ClassGraphUtil.findTargetAnnotationInstancesAnywhere(scanResult, TestCase.class, true);
            annotationList
                .stream()
                .map(extendedAnnotationInfo -> {
                    AnnotationInfo annotationInfo = extendedAnnotationInfo.getAnnotationInfo();
                    ClassInfo declaringClassInfo = extendedAnnotationInfo.getDeclaringClass();
                    String module = ModulePathResolver.computeModulePathString(declaringClassInfo.getName());

                    AnnotationParameterValueList parameterValues = annotationInfo.getParameterValues();
                    String action = (String) parameterValues.get("action").getValue();
                    String[] targets = (String[]) parameterValues.get("targets").getValue();

                    StringBuilder sb = new StringBuilder();
                    sb.append("""
                        [Test Case]
                        - Module: %s
                        - Action: **%s**
                        """.formatted(module, action));
                    Arrays.stream(targets)
                        .forEach(target -> {
                            sb.append("- Target: %s".formatted(target));
                            sb.append("\n");
                        });
                    return sb;
                })
                .sorted()
                .forEach(writer::println);
        }
    }

    @SneakyThrows(IOException.class)
    private static void generateArgumentTypeAdapterGraphFile(ScanResult scanResult) {
        File argumentAdapterGraphFile = TestUtil.COMPILE_TIME_GRAPH_PATH.resolve(ReflectionUtil.CompileTimeGraph.ARGUMENT_TYPE_ADAPTER_GRAPH_FILE_NAME).toFile();
        try (PrintWriter writer = new PrintWriter(argumentAdapterGraphFile, StandardCharsets.UTF_8)) {
            scanResult
                .getSubclasses(BaseArgumentTypeAdapter.class)
                .getNames()
                .stream()
                .sorted()
                .forEach(writer::println);
        }
    }


    @SneakyThrows(IOException.class)
    private static void generateModuleInitializerGraphFile(ScanResult scanResult) {
        File moduleInitializerGraphFile = TestUtil.COMPILE_TIME_GRAPH_PATH.resolve(ReflectionUtil.CompileTimeGraph.MODULE_INITIALIZER_GRAPH_FILE_NAME).toFile();
        try (PrintWriter writer = new PrintWriter(moduleInitializerGraphFile, StandardCharsets.UTF_8)) {
            scanResult
                .getSubclasses(ModuleInitializer.class)
                .getNames()
                .stream()
                .sorted()
                .forEach(writer::println);
        }
    }

    private void searchDefinedModules(JsonObject parent, String level, List<String> result) {
        /* Go down. */
        parent
            .keySet()
            .stream()
            .filter(key -> parent.get(key).isJsonObject())
            .forEach(key -> searchDefinedModules(parent.getAsJsonObject(key), StringUtil.trimPathString(level + "." + key), result));

        /* Go up. */
        if (parent.has(ModuleLoadDeterminer.ENABLE_JSON_KEY)) {
            result.add(level);
        }
    }

    @SneakyThrows(IOException.class)
    @Test
    public void generateModulesGraphFile() {
        /* Generate module-graph.txt file. */
        JsonObject modules = GsonMapper.toJsonTree(new MainControlConfigModel())
            .getAsJsonObject().getAsJsonObject("modules");
        ArrayList<String> result = new ArrayList<>();
        searchDefinedModules(modules, "", result);
        result.sort(String::compareTo);

        Path moduleGraphFile = TestUtil.COMPILE_TIME_GRAPH_PATH.resolve(ReflectionUtil.CompileTimeGraph.MODULE_GRAPH_FILE_NAME);
        Files.createDirectories(moduleGraphFile.getParent());
        try (PrintWriter writer = new PrintWriter(moduleGraphFile.toFile(), StandardCharsets.UTF_8)) {
            result.forEach(writer::println);
        }
    }

}
