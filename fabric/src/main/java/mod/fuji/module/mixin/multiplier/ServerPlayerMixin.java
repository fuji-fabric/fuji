package mod.fuji.module.mixin.multiplier;

import mod.fuji.module.initializer.multiplier.MultiplierInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin {

    @Unique
    @NotNull
    final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

    @ModifyVariable(method = "addExperience", at = @At(value = "HEAD"), argsOnly = true)
    public int multiplyExperience(int exp) {
        exp = (int) MultiplierInitializer.transform(player, "experience", "all", exp);
        return exp;
    }

}
