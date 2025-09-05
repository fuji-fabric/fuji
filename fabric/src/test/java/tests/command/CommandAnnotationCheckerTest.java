package tests.command;

import auxiliary.classgraph.ClassGraphUtil;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.ScanResult;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CommandAnnotationCheckerTest {

    @Test
    public void testCommandAnnotations() {
        try (ScanResult scanResult = ClassGraphUtil
            .makeBaseClassGraph()
            .enableAllInfo()
            .scan()) {

            checkCommandMethodNamingConvention(scanResult);
        }

    }

    private static void checkCommandMethodNamingConvention(ScanResult scanResult) {
        List<MethodInfo> violationMethodInfoList = scanResult
            .getClassesWithMethodAnnotation(CommandNode.class)
            .stream()
            .map(ClassInfo::getMethodInfo)
            .flatMap(methodInfoList -> methodInfoList
                .stream()
                .filter(methodInfo -> methodInfo.hasAnnotation(CommandNode.class)))
            .filter(methodInfo -> !methodInfo.getName().startsWith("$"))
            .toList();

        if (!violationMethodInfoList.isEmpty()) {
            LogUtil.warn("==== The following methods violates the command method naming convention ===");
            violationMethodInfoList
                .forEach(methodInfo -> LogUtil.info("class = {}, methodName = {}", methodInfo.getClassName(), methodInfo.getName()));
            throw new RuntimeException("The command method should follow the naming convention.");
        }
    }

}
