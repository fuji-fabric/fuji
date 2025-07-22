package auxiliary;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationInfoList;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodParameterInfo;
import io.github.classgraph.ScanResult;
import io.github.sakurawald.fuji.Fuji;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class TestUtil {

    public static final Path PROJECT_ROOT_PATH = Path.of("../");
    public static final String ROOT_PACKAGE_NAME = Fuji.class.getPackageName();

    public static ClassGraph makeBaseClassGraph() {
        return new ClassGraph()
            .acceptPackages(ROOT_PACKAGE_NAME);
    }

    @SuppressWarnings("SameParameterValue")
    public static List<String> extractMatches(Pattern pattern, String text, int group) {
        Matcher matcher = pattern.matcher(text);

        List<String> ret = new ArrayList<>();
        while (matcher.find()) {
            ret.add(matcher.group(group));
        }

        return ret;
    }

    @SuppressWarnings("CollectionAddAllCanBeReplacedWithConstructor")
    public static List<AnnotationInfo> findTargetAnnotationInstancesAnywhere(ScanResult scanResult, Class<? extends Annotation> targetAnnotation, boolean isRepeatableAnnotation) {
        List<AnnotationInfo> targetAnnotationInstanceAnywhere = new ArrayList<>();

        /* Collect target annotation instances on class. */
        List<AnnotationInfo> targetAnnotationOnClass =
            scanResult
                .getClassesWithAnnotation(targetAnnotation)
                .stream()
                .flatMap(classInfo -> {
                    if (isRepeatableAnnotation) {
                        AnnotationInfoList annotationInfoRepeatable = classInfo.getAnnotationInfoRepeatable(targetAnnotation);
                        return annotationInfoRepeatable.stream();
                    } else {
                        return Stream.of(classInfo.getAnnotationInfo(targetAnnotation));
                    }
                })
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
                .flatMap(fieldInfo -> {
                    if (isRepeatableAnnotation) {
                        return fieldInfo.getAnnotationInfoRepeatable(targetAnnotation).stream();
                    } else {
                        return Stream.of(fieldInfo.getAnnotationInfo(targetAnnotation));
                    }
                })
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
            .flatMap(methodInfo -> {
                if (isRepeatableAnnotation) {
                    return methodInfo.getAnnotationInfoRepeatable(targetAnnotation).stream();
                } else {
                    return Stream.of(methodInfo.getAnnotationInfo(targetAnnotation));
                }
            })
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
            .flatMap(methodParameterInfo -> {
                if (isRepeatableAnnotation) {
                    return methodParameterInfo.getAnnotationInfoRepeatable(targetAnnotation).stream();
                } else {
                    return Stream.of(methodParameterInfo.getAnnotationInfo(targetAnnotation));
                }
            })
            .toList();

        targetAnnotationInstanceAnywhere.addAll(targetAnnotationOnMethodParameters);

        /* Return the result. */
        return targetAnnotationInstanceAnywhere;
    }
}
