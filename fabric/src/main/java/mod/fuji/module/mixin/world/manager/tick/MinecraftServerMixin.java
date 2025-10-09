package mod.fuji.module.mixin.world.manager.tick;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mod.fuji.module.initializer.world.manager.structure.util.SafeIterator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.Iterator;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @SuppressWarnings("unchecked")
    @WrapOperation(method = "tickWorlds", at = @At(value = "INVOKE", target = "Ljava/lang/Iterable;iterator()Ljava/util/Iterator;", ordinal = 0), require = 0)
    private @NotNull Iterator<ServerWorld> fuji$copyBeforeTicking(Iterable<?> instance, Operation<Iterator<?>> original) {
        // NOTE: After issue /world reset, it's possible that all the worlds will be ticked twice.
        return new SafeIterator<>((Collection<ServerWorld>) instance);
    }
}
