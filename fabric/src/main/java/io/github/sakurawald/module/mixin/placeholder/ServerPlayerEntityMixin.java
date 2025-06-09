package io.github.sakurawald.module.mixin.placeholder;

import io.github.sakurawald.module.initializer.placeholder.structure.SumUpPlaceholder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "increaseStat(Lnet/minecraft/stat/Stat;I)V", at = @At("HEAD"))
    public void syncSumUpPlaceholderInMemory(@NotNull Stat<?> stat, int i, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (Stats.KILLED.equals(stat.getType())) {
            SumUpPlaceholder.ofPlayer(player.getUuidAsString()).killed += i;
        } else if (Stats.USED.equals(stat.getType())) {
            SumUpPlaceholder.ofPlayer(player.getUuidAsString()).placed += i;
        } else if (Stats.MINED.equals(stat.getType())) {
            SumUpPlaceholder.ofPlayer(player.getUuidAsString()).mined += i;
        }
    }
}
