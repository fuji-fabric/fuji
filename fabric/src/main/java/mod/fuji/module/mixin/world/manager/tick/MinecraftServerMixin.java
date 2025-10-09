package mod.fuji.module.mixin.world.manager.tick;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mod.fuji.module.initializer.world.manager.structure.util.SafeIterator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Iterator;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @SuppressWarnings("unchecked")
    @ModifyExpressionValue(method = "tickWorlds", at = @At("MIXINEXTRAS:EXPRESSION"))
    @Expression("this.getWorlds().iterator()")
    @Definition(id = "getWorlds", method = "Lnet/minecraft/server/MinecraftServer;getWorlds()Ljava/lang/Iterable;")
    @Definition(id = "iterator", method = "Ljava/lang/Iterable;iterator()Ljava/util/Iterator;")
    @NotNull Iterator<ServerWorld> copyBeforeTickWorlds(Iterator<?> original) {
        // NOTE: After issue /world reset, it's possible that all the worlds will be ticked twice.
        return new SafeIterator<>((Iterator<ServerWorld>) original);
    }

}
