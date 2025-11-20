package mod.fuji.module.mixin.world.manager.tick;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mod.fuji.module.initializer.world.manager.structure.util.SafeIterator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @ModifyExpressionValue(method = "tickChildren", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getAllLevels()Ljava/lang/Iterable;"))
    private Iterable<ServerLevel> fuji$copyBeforeTicking(Iterable<ServerLevel> original) {
        // NOTE: After issue /world reset, it's possible that all the worlds will be ticked twice.
        return () -> new SafeIterator<>(original);
    }

}
