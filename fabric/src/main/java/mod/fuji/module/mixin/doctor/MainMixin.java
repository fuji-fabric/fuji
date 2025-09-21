package mod.fuji.module.mixin.doctor;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mod.fuji.module.initializer.doctor.DoctorInitializer;
import net.minecraft.server.Main;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Main.class)
public class MainMixin {

    @WrapOperation(method = "main",
        at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Lorg/slf4j/Marker;Ljava/lang/String;Ljava/lang/Throwable;)V"),
        remap = false,
        require = 0 // Ouch, I need another doctor to save the doctor.
    )
    private static void captureExceptionInMainThread(Logger instance, Marker marker, String s, Throwable throwable, Operation<Void> original) {
        /* Call original first. */
        original.call(instance, marker, s, throwable);

        /* Analyze the captured throwable. */
        DoctorInitializer.analyzeCaptureThrowable(throwable);
    }
}
