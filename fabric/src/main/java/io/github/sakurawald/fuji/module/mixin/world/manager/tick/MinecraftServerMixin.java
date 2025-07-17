package io.github.sakurawald.fuji.module.mixin.world.manager.tick;

import io.github.sakurawald.fuji.module.initializer.world.manager.structure.registry.SafeIterator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;
import java.util.Iterator;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Redirect(method = "tickWorlds", at = @At(value = "INVOKE", target = "Ljava/lang/Iterable;iterator()Ljava/util/Iterator;", ordinal = 0), require = 0)
    private @NotNull Iterator<ServerWorld> fuji$copyBeforeTicking(Iterable<ServerWorld> instance) {
        // NOTE: After issue /world reset, it's possible that all the worlds will be ticked twice.
        return new SafeIterator<>((Collection<ServerWorld>) instance);
    }
}
