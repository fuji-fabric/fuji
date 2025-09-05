package auxiliary.classgraph;

import auxiliary.classgraph.structure.ExtendedAnnotationInfo;
import io.github.classgraph.AnnotationInfoList;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.MethodParameterInfo;
import io.github.classgraph.ScanResult;
import io.github.sakurawald.fuji.Fuji;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public class ClassGraphUtil {
    private static final String ROOT_PACKAGE_NAME = Fuji.class.getPackageName();

    public static ClassGraph makeBaseClassGraph() {
        return new ClassGraph()
            .acceptPackages(ROOT_PACKAGE_NAME);
    }

    public static ScanResult makeScanResult() {
        return makeBaseClassGraph()
            .enableAllInfo()
            .scan();
    }


    @SuppressWarnings("CollectionAddAllCanBeReplacedWithConstructor")
    public static List<ExtendedAnnotationInfo> findTargetAnnotationInstancesAnywhere(@NotNull ScanResult scanResult, @NotNull Class<? extends Annotation> targetAnnotation, boolean isRepeatableAnnotation) {
        List<ExtendedAnnotationInfo> targetAnnotationInstanceAnywhere = new ArrayList<>();

        /* Collect target annotation instances on class. */
        List<ExtendedAnnotationInfo> targetAnnotationOnClass =
            scanResult
                .getClassesWithAnnotation(targetAnnotation)
                .stream()
                .flatMap(classInfo -> {
                    if (isRepeatableAnnotation) {
                        AnnotationInfoList annotationInfoRepeatable = classInfo.getAnnotationInfoRepeatable(targetAnnotation);
                        return annotationInfoRepeatable
                            .stream()
                            .map(it -> new ExtendedAnnotationInfo(it, classInfo));
                    } else {
                        ExtendedAnnotationInfo singularElement = new ExtendedAnnotationInfo(classInfo.getAnnotationInfo(targetAnnotation), classInfo);
                        return Stream.of(singularElement);
                    }
                })
                .toList();
        targetAnnotationInstanceAnywhere.addAll(targetAnnotationOnClass);

        /* Collect target annotation instances on field. */
        List<ExtendedAnnotationInfo> targetAnnotationOnField =
            scanResult
                .getClassesWithFieldAnnotation(targetAnnotation)
                .stream()
                .map(ClassInfo::getDeclaredFieldInfo)
                .flatMap(fieldInfoList ->
                    fieldInfoList
                        .stream()
                        .filter(fieldInfo -> fieldInfo.hasAnnotation(targetAnnotation)))
                .flatMap(fieldInfo -> {
                    ClassInfo classInfo = fieldInfo.getClassInfo();
                    if (isRepeatableAnnotation) {
                        return fieldInfo.getAnnotationInfoRepeatable(targetAnnotation)
                            .stream()
                            .map(it -> new ExtendedAnnotationInfo(it, classInfo));
                    } else {
                        ExtendedAnnotationInfo singularElement = new ExtendedAnnotationInfo(fieldInfo.getAnnotationInfo(targetAnnotation), classInfo);
                        return Stream.of(singularElement);
                    }
                })
                .toList();
        targetAnnotationInstanceAnywhere.addAll(targetAnnotationOnField);

        /* Collect target annotation instances on method. */
        List<ExtendedAnnotationInfo> targetAnnotationOnMethod = scanResult
            .getClassesWithMethodAnnotation(targetAnnotation)
            .stream()
            .map(ClassInfo::getDeclaredMethodInfo)
            .flatMap(methodInfoList ->
                methodInfoList
                    .stream()
                    .filter(methodInfo -> methodInfo.hasAnnotation(targetAnnotation)))
            .flatMap(methodInfo -> {
                ClassInfo classInfo = methodInfo.getClassInfo();
                if (isRepeatableAnnotation) {
                    return methodInfo.getAnnotationInfoRepeatable(targetAnnotation)
                        .stream()
                        .map(it -> new ExtendedAnnotationInfo(it, classInfo, methodInfo));
                } else {
                    ExtendedAnnotationInfo singularElement = new ExtendedAnnotationInfo(methodInfo.getAnnotationInfo(targetAnnotation), classInfo, methodInfo);
                    return Stream.of(singularElement);
                }
            })
            .toList();
        targetAnnotationInstanceAnywhere.addAll(targetAnnotationOnMethod);

        /* Collect target annotation instances on method parameters. */
        List<ExtendedAnnotationInfo> targetAnnotationOnMethodParameters = scanResult
            .getClassesWithMethodParameterAnnotation(targetAnnotation)
            .stream()
            .map(ClassInfo::getDeclaredMethodInfo)
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
                MethodInfo methodInfo = methodParameterInfo.getMethodInfo();
                ClassInfo classInfo = methodInfo.getClassInfo();
                if (isRepeatableAnnotation) {
                    return methodParameterInfo.getAnnotationInfoRepeatable(targetAnnotation)
                        .stream()
                        .map(it -> new ExtendedAnnotationInfo(it, classInfo, methodInfo));
                } else {
                    ExtendedAnnotationInfo singularElement = new ExtendedAnnotationInfo(methodParameterInfo.getAnnotationInfo(targetAnnotation), classInfo, methodInfo);
                    return Stream.of(singularElement);
                }
            })
            .toList();

        targetAnnotationInstanceAnywhere.addAll(targetAnnotationOnMethodParameters);

        /* Return the result. */
        return targetAnnotationInstanceAnywhere;
    }
}
