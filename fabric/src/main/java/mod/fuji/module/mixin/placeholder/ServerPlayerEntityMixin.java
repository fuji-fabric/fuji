package mod.fuji.module.mixin.placeholder;

import mod.fuji.module.initializer.placeholder.structure.SumUpPlaceholder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "awardStat(Lnet/minecraft/stats/Stat;I)V", at = @At("HEAD"))
    void syncSumUpPlaceholderInMemory(@NotNull Stat<?> stat, int i, CallbackInfo ci) {
        Player player = (Player) (Object) this;

        if (Stats.ENTITY_KILLED.equals(stat.getType())) {
            SumUpPlaceholder.ofPlayer(player.getStringUUID()).killed += i;
        } else if (Stats.ITEM_USED.equals(stat.getType())) {
            SumUpPlaceholder.ofPlayer(player.getStringUUID()).placed += i;
        } else if (Stats.BLOCK_MINED.equals(stat.getType())) {
            SumUpPlaceholder.ofPlayer(player.getStringUUID()).mined += i;
        }
    }
}
