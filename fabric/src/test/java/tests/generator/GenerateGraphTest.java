package tests.generator;

import auxiliary.classgraph.ClassGraphUtil;
import auxiliary.TestUtil;
import auxiliary.classgraph.structure.ExtendedAnnotationInfo;
import com.google.gson.JsonObject;
import io.github.classgraph.AnnotationClassRef;
import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationParameterValueList;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.MethodParameterInfo;
import io.github.classgraph.ScanResult;
import io.github.sakurawald.fuji.core.auxiliary.JsonUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.auxiliary.StringUtil;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.config.mapper.GsonMapper;
import io.github.sakurawald.fuji.core.config.model.MainControlConfigModel;
import io.github.sakurawald.fuji.core.document.annotation.Cite;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.inject.structure.EventConsumerInfo;
import io.github.sakurawald.fuji.core.event.inject.structure.EventConsumerInfoList;
import io.github.sakurawald.fuji.core.event.inject.structure.EventGraph;
import io.github.sakurawald.fuji.core.event.inject.structure.EventProducerInfo;
import io.github.sakurawald.fuji.core.event.inject.structure.EventProducerInfoList;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class GenerateGraphTest {

    public static final Path COMPILE_TIME_RESOURCE_PATH = TestUtil.ROOT_PROJECT_ROOT_PATH.resolve("common/src/main/resources/");
    public static final Path COMPILE_TIME_GRAPH_PATH = COMPILE_TIME_RESOURCE_PATH.resolve(ReflectionUtil.CompileTimeGraph.GRAPH_DIRECTORY_NAME);
    public static final Path COMPILE_TIME_CITE_FILE_PATH = TestUtil.ROOT_PROJECT_ROOT_PATH.resolve("CITE.md");
    public static final Path COMPILE_TIME_TEST_CASE_FILE_PATH = TestUtil.ROOT_PROJECT_ROOT_PATH.resolve("TEST-CASE.md");

    @Test
    @SneakyThrows(IOException.class)
    public void generateBitsFromCodebase() {
        /* Scan the codebase. */
        try (ScanResult scanResult = ClassGraphUtil
            .makeBaseClassGraph()
            .enableAllInfo()
            .scan()) {

            Files.createDirectories(COMPILE_TIME_GRAPH_PATH);

            /* Generate module-initializer-graph.txt file. */
            generateModuleInitializerGraphFile(scanResult);

            /* Generate argument-type-adapter-graph.txt file. */
            generateArgumentTypeAdapterGraphFile(scanResult);

            /* Generate event-graph.json file. */
            generateEventGraphFile(scanResult);

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
                    String module = ModuleManager.computeJoinedModulePath(declaringClassInfo.getName());

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
        File argumentAdapterGraphFile = COMPILE_TIME_GRAPH_PATH.resolve(ReflectionUtil.CompileTimeGraph.ARGUMENT_TYPE_ADAPTER_GRAPH_FILE_NAME).toFile();
        try (PrintWriter writer = new PrintWriter(argumentAdapterGraphFile, StandardCharsets.UTF_8)) {
            scanResult
                .getSubclasses(BaseArgumentTypeAdapter.class)
                .getNames()
                .stream()
                .sorted()
                .forEach(writer::println);
        }
    }

    private void generateEventGraphFile(ScanResult scanResult) {
        Path graphFilePath = COMPILE_TIME_GRAPH_PATH.resolve(ReflectionUtil.CompileTimeGraph.EVENT_GRAPH_FILE_NAME);

        EventGraph eventGraph = new EventGraph();

        collectEventProducers(scanResult, eventGraph);
        collectEventConsumers(scanResult, eventGraph);
        sortEventGraph(eventGraph);

        JsonObject eventGraphJsonObject = GsonMapper.toJsonTree(eventGraph).getAsJsonObject();
        JsonUtil.writeJsonObject(eventGraphJsonObject, graphFilePath);
    }

    private void sortEventGraph(EventGraph eventGraph) {
        eventGraph
            .getConsumers()
            .values()
            .forEach(consumer -> consumer.sort(
                Comparator
                    .comparing(EventConsumerInfo::getInjectorPriority)
                    .thenComparing(EventConsumerInfo::getConsumerPriority)));
    }

    @SuppressWarnings("unused")
    private static void collectEventProducers(ScanResult scanResult, EventGraph eventGraph) {
        List<ExtendedAnnotationInfo> extendedAnnotationInfoList = ClassGraphUtil.findTargetAnnotationInstancesAnywhere(scanResult, EventProducer.class, false);
        extendedAnnotationInfoList.forEach(extendedAnnotationInfo -> {

            String declaringClassName = extendedAnnotationInfo.getDeclaringClass().getName();
            MethodInfo declaringMethod = extendedAnnotationInfo.getDeclaringMethod();
            String declaringMethodName = declaringMethod.getName();

            AnnotationParameterValueList parameterValues = extendedAnnotationInfo.getAnnotationInfo().getParameterValues();

            AnnotationClassRef annotationClassRef = (AnnotationClassRef) parameterValues.getValue("value");
            String eventName = annotationClassRef.getClassInfo().getName();

            int injectorPriority = (int) parameterValues.getValue("injectorPriority");

            EventProducerInfo eventProducerInfo = new EventProducerInfo(declaringClassName, declaringMethodName, injectorPriority);

            eventGraph
                .getProducers()
                .computeIfAbsent(eventName, k -> new EventProducerInfoList())
                .add(eventProducerInfo);
        });
    }

    private static void collectEventConsumers(ScanResult scanResult, EventGraph eventGraph) {
        List<ExtendedAnnotationInfo> extendedAnnotationInfoList = ClassGraphUtil.findTargetAnnotationInstancesAnywhere(scanResult, EventConsumer.class, false);
        extendedAnnotationInfoList.forEach(extendedAnnotationInfo -> {

            String declaringClassName = extendedAnnotationInfo.getDeclaringClass().getName();
            MethodInfo declaringMethod = extendedAnnotationInfo.getDeclaringMethod();
            String declaringMethodName = declaringMethod.getName();

            MethodParameterInfo[] parameterInfo = declaringMethod.getParameterInfo();
            if (parameterInfo.length != 1) {
                throw new IllegalArgumentException("Expecting exactly one parameter in method annotated with @EventHandler annotation.");
            }

            String resultType = declaringMethod.getTypeDescriptor().getResultType().toString();
            if (!resultType.equals("void")) {
                throw new IllegalArgumentException("The type of return value in method annotated with @EventHandler annotation must be 'void'.");
            }

            if (!declaringMethod.isStatic()) {
                throw new IllegalArgumentException("The method annotated with @EventHandler annotation must be 'static'.");
            }

            String eventName = parameterInfo[0].getTypeDescriptor().toString();
            if (!eventGraph.getProducers().containsKey(eventName)) {
                throw new IllegalArgumentException("There is no event producer for the event type: " + eventName);
            }

            int injectorPriority = (int) extendedAnnotationInfo.getAnnotationInfo().getParameterValues().getValue("injectorPriority");
            int consumerPriority = (int) extendedAnnotationInfo.getAnnotationInfo().getParameterValues().getValue("consumerPriority");

            EventConsumerInfo eventConsumerInfo = new EventConsumerInfo(declaringClassName, declaringMethodName, injectorPriority, consumerPriority);
            eventGraph
                .getConsumers()
                .computeIfAbsent(eventName, k -> new EventConsumerInfoList())
                .add(eventConsumerInfo);
        });
    }


    @SneakyThrows(IOException.class)
    private static void generateModuleInitializerGraphFile(ScanResult scanResult) {
        File moduleInitializerGraphFile = COMPILE_TIME_GRAPH_PATH.resolve(ReflectionUtil.CompileTimeGraph.MODULE_INITIALIZER_GRAPH_FILE_NAME).toFile();
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
        if (parent.has(ModuleManager.ENABLE_SUPPLIER_KEY)) {
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

        Path moduleGraphFile = COMPILE_TIME_GRAPH_PATH.resolve(ReflectionUtil.CompileTimeGraph.MODULE_GRAPH_FILE_NAME);
        Files.createDirectories(moduleGraphFile.getParent());
        try (PrintWriter writer = new PrintWriter(moduleGraphFile.toFile(), StandardCharsets.UTF_8)) {
            result.forEach(writer::println);
        }
    }

}
