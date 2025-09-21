package tests.mixin;

import auxiliary.classgraph.ClassGraphUtil;
import auxiliary.classgraph.structure.ExtendedAnnotationInfo;
import mod.fuji.core.auxiliary.LogUtil;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.injection.Redirect;

public class CheckMixinInjectorTest {

    @Test
    void checkMixinInjectors() {
        banRedirectInjector();
    }

    void banRedirectInjector() {
        ClassGraphUtil.withScanResult(scanResult -> {
            List<ExtendedAnnotationInfo> result = ClassGraphUtil.findTargetAnnotationInstancesAnywhere(scanResult, Redirect.class, false);
            if (!result.isEmpty()) {
                result.forEach(it -> {
                    LogUtil.error("Found @Redirect injector in class {}", it.getDeclaringClass().getName());
                });

                throw new RuntimeException("Use @WrapOperation instead of @Redirect.");
            }
        });
    }


}
