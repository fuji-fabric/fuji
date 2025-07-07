package tests;

import auxiliary.TestUtility;
import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationParameterValueList;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodParameterInfo;
import io.github.classgraph.ScanResult;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.structure.DocString;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class GenerateDocStringTest {

    @Test
    void generateDocStringListInRuntimeEnvironment() {
        try (ScanResult scanResult = TestUtility.makeBaseClassGraph()
            .enableAllInfo()
            .scan()) {

            /* Make the doc string list. */
            List<DocString> docStringList = makeDocStringFromDocumentAnnotation(scanResult);
            LogUtil.info("There are {} doc strings.", docStringList.size());

            /* Check duplicated doc string. */
            checkDuplicateDocString(docStringList);
        }
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
