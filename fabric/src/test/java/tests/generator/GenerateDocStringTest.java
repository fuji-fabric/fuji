package tests.generator;

import auxiliary.classgraph.ClassGraphUtil;
import auxiliary.TestUtil;
import auxiliary.classgraph.structure.ExtendedAnnotationInfo;
import com.google.gson.JsonObject;
import io.github.classgraph.AnnotationParameterValueList;
import io.github.classgraph.ScanResult;
import io.github.sakurawald.fuji.core.auxiliary.JsonUtil;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.config.mapper.GsonMapper;
import io.github.sakurawald.fuji.core.config.handler.impl.LanguageConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.structure.DocString;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class GenerateDocStringTest {

    private static final Path COMPILE_TIME_CROWDIN_PATH = TestUtil.ROOT_PROJECT_ROOT_PATH.resolve("crowdin/");
    private static final Path COMPILE_TIME_PULL_FROM_CROWDIN_LANGUAGE_PATH = COMPILE_TIME_CROWDIN_PATH.resolve("pull-from-crowdin/");
    private static final Path COMPILE_TIME_PUSH_TO_CROWDIN_LANGUAGE_PATH = COMPILE_TIME_CROWDIN_PATH.resolve("push-to-crowdin/");
    private static final Path COMPILE_TIME_DEFAULT_LANGUAGE_FILE_PATH = COMPILE_TIME_PUSH_TO_CROWDIN_LANGUAGE_PATH.resolve("en_US.json");

    @Test
    @SneakyThrows(IOException.class)
    public void generateLanguageGraphFile() {
        /* Generate language-graph.txt file. */
        File languageGraphFile = GenerateGraphTest.COMPILE_TIME_GRAPH_PATH.resolve(ReflectionUtil.CompileTimeGraph.LANGUAGE_GRAPH_FILE_NAME).toFile();
        try (PrintWriter writer = new PrintWriter(languageGraphFile, StandardCharsets.UTF_8)) {
            File languageFilesPath = GenerateDocStringTest.COMPILE_TIME_PULL_FROM_CROWDIN_LANGUAGE_PATH.toFile();
            Arrays
                .stream(Objects.requireNonNull(languageFilesPath.listFiles()))
                .map(File::getName)
                .forEach(writer::println);
        }
    }

    @SneakyThrows(IOException.class)
    @Test
    public void generateDocStringListInRuntimeEnvironment() {
        try (ScanResult scanResult = ClassGraphUtil.makeBaseClassGraph()
            .enableAllInfo()
            .scan()) {

            /* Make the doc string list. */
            List<DocString> docStringList = new ArrayList<>();
            docStringList.addAll(makeDocStringFromDocumentAnnotation(scanResult));
            docStringList.addAll(makeDocStringFromColorBoxAnnotation(scanResult));
            docStringList.addAll(makeDocStringFromDocStringProviderAnnotation(scanResult));
            LogUtil.info("There are {} doc strings.", docStringList.size());

            /* Remove test purpose doc string. */
            docStringList.removeIf(docString -> docString.getId() == 0L);

            /* Check duplicated doc string. */
            checkDuplicateDocString(docStringList);

            /* Check invalid doc string. */
            checkInvalidDocString(docStringList);

            /* Override the doc string into the default language file. */
            writeDocStringListIntoDefaultLanguageFile(docStringList);
        }

    }

    private List<DocString> makeDocStringFromDocStringProviderAnnotation(ScanResult scanResult) {
        return ClassGraphUtil.findTargetAnnotationInstancesAnywhere(scanResult, DocStringProvider.class, true)
            .stream()
            .map(ExtendedAnnotationInfo::getAnnotationInfo)
            .map(annotationInfo -> {
                AnnotationParameterValueList parameterValues = annotationInfo.getParameterValues();
                long id = (long) parameterValues.getValue("id");
                String value = (String) parameterValues.getValue("value");
                return new DocString(id, value);
            })
            .toList();
    }

    private static void writeDocStringListIntoDefaultLanguageFile(List<DocString> docStringList) throws IOException {
        /* Read the default language json. */
        JsonObject defaultLanguageJson = JsonUtil.readJsonFile(COMPILE_TIME_DEFAULT_LANGUAGE_FILE_PATH).getAsJsonObject();
        if (JsonUtil.isEmpty(defaultLanguageJson)) {
            throw new RuntimeException("Default language file is empty.");
        }

        /* Remove un-used doc strings. */
        defaultLanguageJson
            .keySet()
            .removeIf(key -> key.startsWith(DocString.DOC_STRING_KEY_PREFIX));

        /* Append the doc string into the default language json. */
        for (DocString docString : docStringList) {
            String jsonKey = DocString.DOC_STRING_KEY_PREFIX + docString.getId();
            String jsonValue = docString.getValue();
            defaultLanguageJson.addProperty(jsonKey, jsonValue);
        }
        LogUtil.info("Write {} doc strings into the default language file.", docStringList.size());

        /* Sort the json. */
        defaultLanguageJson = LanguageConfigurationHandler.makeSortedLanguageJsonObject(defaultLanguageJson);

        /* Override the default language file. */
        String jsonString = GsonMapper.toJsonString(defaultLanguageJson);
        Files.writeString(COMPILE_TIME_DEFAULT_LANGUAGE_FILE_PATH, jsonString);
    }

    private static @NotNull List<DocString> makeDocStringFromDocumentAnnotation(ScanResult scanResult) {
        return ClassGraphUtil.findTargetAnnotationInstancesAnywhere(scanResult, Document.class, false)
            .stream()
            .map(ExtendedAnnotationInfo::getAnnotationInfo)
            .map(annotationInfo -> {
                AnnotationParameterValueList parameterValues = annotationInfo.getParameterValues();
                long id = (long) parameterValues.getValue("id");
                String value = (String) parameterValues.getValue("value");
                return new DocString(id, value);
            })
            .toList();
    }

    private @NotNull List<DocString> makeDocStringFromColorBoxAnnotation(ScanResult scanResult) {
        return ClassGraphUtil.findTargetAnnotationInstancesAnywhere(scanResult, ColorBox.class, true)
            .stream()
            .map(ExtendedAnnotationInfo::getAnnotationInfo)
            .map(annotationInfo -> {
                AnnotationParameterValueList parameterValues = annotationInfo.getParameterValues();
                long id = (long) parameterValues.getValue("id");
                String value = (String) parameterValues.getValue("value");
                return new DocString(id, value);
            })
            .toList();
    }


    private static void checkDuplicateDocString(List<DocString> docStringList) {
        List<Long> duplicateIds = docStringList
            .stream()
            .collect(Collectors.groupingBy(DocString::getId, Collectors.counting()))
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() > 1)
            .map(Map.Entry::getKey)
            .toList();

        LogUtil.info("There are {} duplicate doc strings. (IDs = {})", duplicateIds.size(), duplicateIds);
        if (!duplicateIds.isEmpty()) {
            throw new RuntimeException("Duplicated IDs: " + duplicateIds);
        }
    }

    private static void checkInvalidDocString(List<DocString> docStringList) {
        List<DocString> invalidDocStringList = docStringList
            .stream()
            .filter(docString -> {
                long id = docString.getId();
                return id < 1735689600000L || id > 2051222400000L;
            })
            .toList();

        if (!invalidDocStringList.isEmpty()) {
            throw new RuntimeException("Invalid IDs: " + invalidDocStringList);
        }
    }

}
