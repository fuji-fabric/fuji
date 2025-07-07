package tests;

import auxiliary.TestUtil;
import com.google.gson.JsonObject;
import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationParameterValueList;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodParameterInfo;
import io.github.classgraph.ScanResult;
import io.github.sakurawald.fuji.core.auxiliary.JsonUtil;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.auxiliary.DocumentUtil;
import io.github.sakurawald.fuji.core.document.structure.DocString;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class GenerateDocStringTest {

    @SuppressWarnings("CollectionAddAllCanBeReplacedWithConstructor")
    @SneakyThrows
    @Test
    public void generateDocStringListInRuntimeEnvironment() {
        try (ScanResult scanResult = TestUtil.makeBaseClassGraph()
            .enableAllInfo()
            .scan()) {

            /* Make the doc string list. */
            List<DocString> docStringList = new ArrayList<>();
            docStringList.addAll(makeDocStringFromDocumentAnnotation(scanResult));
            LogUtil.info("There are {} doc strings.", docStringList.size());

            /* Check duplicated doc string. */
            checkDuplicateDocString(docStringList);

            /* Check invalid doc string. */
            checkInvalidDocString(docStringList);

            /* Override the doc string into the default language file. */
            writeDocStringListIntoDefaultLanguageFile(docStringList);
        }

    }

    private static void writeDocStringListIntoDefaultLanguageFile(List<DocString> docStringList) throws IOException {
        /* Read the default language json. */
        Path defaultLanguageFilePath = GenerateGraphTest.COMPILE_TIME_PUSH_TO_CROWDIN_LANGUAGE_PATH.resolve("en_US.json");
        JsonObject defaultLanguageJson = TestUtil.readJsonElement(defaultLanguageFilePath).getAsJsonObject();
        if (JsonUtil.isEmpty(defaultLanguageJson)) {
            throw new RuntimeException("Default language file is empty.");
        }

        /* Sort the json. */
        defaultLanguageJson = JsonUtil.makeSortedJsonObject(defaultLanguageJson);

        /* Delete the existed docstring keys. */
        defaultLanguageJson.keySet().removeIf(key -> key.startsWith(DocumentUtil.DOC_STRING_KEY_PREFIX));

        /* Append the doc string into the default language json. */
        docStringList.sort(Comparator.comparing(DocString::getId));
        for (DocString docString : docStringList) {
            String jsonKey = DocumentUtil.DOC_STRING_KEY_PREFIX + docString.getId();
            String jsonValue = docString.getValue();
            defaultLanguageJson.addProperty(jsonKey, jsonValue);
        }
        LogUtil.info("Write {} doc strings into the default language file.", docStringList.size());

        /* Override the default language file. */
        String jsonString = BaseConfigurationHandler.getGson().toJson(defaultLanguageJson);
        Files.writeString(defaultLanguageFilePath, jsonString);
    }

    private static @NotNull List<DocString> makeDocStringFromDocumentAnnotation(ScanResult scanResult) {
        return findTargetAnnotationInstancesAnywhere(scanResult, Document.class)
            .stream()
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

    @SuppressWarnings("CollectionAddAllCanBeReplacedWithConstructor")
    private static List<AnnotationInfo> findTargetAnnotationInstancesAnywhere(ScanResult scanResult, Class<? extends Annotation> targetAnnotation) {
        List<AnnotationInfo> targetAnnotationInstanceAnywhere = new ArrayList<>();

        /* Collect target annotation instances on class. */
        List<AnnotationInfo> targetAnnotationOnClass =
            scanResult
                .getClassesWithAnnotation(targetAnnotation)
                .stream()
                .map(classInfo -> classInfo.getAnnotationInfo(targetAnnotation))
                .toList();
        targetAnnotationInstanceAnywhere.addAll(targetAnnotationOnClass);

        /* Collect target annotation instances on field. */
        List<AnnotationInfo> targetAnnotationOnField =
            scanResult
                .getClassesWithFieldAnnotation(targetAnnotation)
                .stream()
                .map(ClassInfo::getDeclaredFieldInfo)
                .flatMap(fieldInfoList ->
                    fieldInfoList
                        .stream()
                        .filter(fieldInfo -> fieldInfo.hasAnnotation(targetAnnotation)))
                .map(fieldInfo -> fieldInfo.getAnnotationInfo(targetAnnotation))
                .toList();
        targetAnnotationInstanceAnywhere.addAll(targetAnnotationOnField);

        /* Collect target annotation instances on method. */
        List<AnnotationInfo> targetAnnotationOnMethod = scanResult
            .getClassesWithMethodAnnotation(targetAnnotation)
            .stream()
            .map(ClassInfo::getMethodInfo)
            .flatMap(methodInfoList ->
                methodInfoList
                    .stream()
                    .filter(methodInfo -> methodInfo.hasAnnotation(targetAnnotation)))
            .map(methodInfo -> methodInfo.getAnnotationInfo(targetAnnotation))
            .toList();
        targetAnnotationInstanceAnywhere.addAll(targetAnnotationOnMethod);

        /* Collect target annotation instances on method parameters. */
        List<AnnotationInfo> targetAnnotationOnMethodParameters = scanResult
            .getClassesWithMethodParameterAnnotation(targetAnnotation)
            .stream()
            .map(ClassInfo::getMethodInfo)
            .flatMap(methodInfoList ->
                methodInfoList
                    .stream()
                    .filter(methodInfo -> methodInfo.hasParameterAnnotation(targetAnnotation)))
            .flatMap(methodInfo -> {
                MethodParameterInfo[] parameterInfo = methodInfo.getParameterInfo();
                return Arrays.stream(parameterInfo);
            })
            .filter(methodParameterInfo -> methodParameterInfo.hasAnnotation(targetAnnotation))
            .map(methodParameterInfo -> methodParameterInfo.getAnnotationInfo(targetAnnotation))
            .toList();

        targetAnnotationInstanceAnywhere.addAll(targetAnnotationOnMethodParameters);

        /* Return the result. */
        return targetAnnotationInstanceAnywhere;
    }


}
