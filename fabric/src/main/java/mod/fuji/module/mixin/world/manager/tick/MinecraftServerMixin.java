package mod.fuji.module.mixin.world.manager.tick;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mod.fuji.module.initializer.world.manager.structure.util.SafeIterator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @ModifyExpressionValue(method = "tickWorlds", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorlds()Ljava/lang/Iterable;"), require = 0)
    private Iterable<ServerWorld> fuji$copyBeforeTicking(Iterable<ServerWorld> original) {
        // NOTE: After issue /world reset, it's possible that all the worlds will be ticked twice.
        return () -> new SafeIterator<>(original);
    }

}
