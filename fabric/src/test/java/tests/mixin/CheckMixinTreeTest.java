package tests.mixin;

import auxiliary.ClassGraphUtil;
import auxiliary.structure.ExtendedAnnotationInfo;
import com.google.errorprone.annotations.Keep;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.ScanResult;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.Mixin;

public class CheckMixinTreeTest {

    @Test
    void checkMixinNamingConvention() {
        try (ScanResult scanResult = ClassGraphUtil
            .makeBaseClassGraph()
            .enableAllInfo()
            .scan()) {

            List<ExtendedAnnotationInfo> mixinClasses = ClassGraphUtil.findTargetAnnotationInstancesAnywhere(scanResult, Mixin.class, false);
            checkMixinClassNamingConvention(mixinClasses);
            checkMixinMethodNamingConvention(mixinClasses);
        }
    }


    private void checkMixinClassNamingConvention(@NotNull List<ExtendedAnnotationInfo> mixinClasses) {
        List<ExtendedAnnotationInfo> violationList = mixinClasses
            .stream()
            .filter(it -> !it.getDeclaringClass().getSimpleName().endsWith("Mixin"))
            .toList();

        if (!violationList.isEmpty()) {
            LogUtil.error("The name of a mixin class should ends with 'Mixin': violationList = {}", violationList.stream().map(it -> it.getDeclaringClass().getName()).toList());
            throw new RuntimeException();
        }

    }

    private void checkMixinMethodNamingConvention(List<ExtendedAnnotationInfo> mixinClasses) {
        List<MethodInfo> violationList = mixinClasses
            .stream()
            .flatMap(mixinClass -> mixinClass.getDeclaringClass().getDeclaredMethodInfo().stream())
            .filter(mixinMethod -> mixinMethod.getAnnotationInfo(Keep.class) == null)
            .filter(mixinMethod -> mixinMethod.getName().length() <= 5)
            .toList();

        if (!violationList.isEmpty()) {
            LogUtil.error("The name of a mixin function should be meaningful: violationList = {}", violationList.stream().map(it -> it.getClassName() + "#" + it.getName()).toList());
            throw new RuntimeException();
        }
    }
}
