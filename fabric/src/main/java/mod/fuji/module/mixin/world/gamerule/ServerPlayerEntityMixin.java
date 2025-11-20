package mod.fuji.module.mixin.world.gamerule;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {

    @ModifyExpressionValue(method = "restoreFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    boolean useOriginalPlayerGameRules(boolean original, @SuppressWarnings("UnresolvedLocalCapture") @Local(argsOnly = true) ServerPlayer oldPlayer) {
        // NOTE: For `keepInventory` game rule. Its value is checked in copyFrom() method after the player is dead. The value comes from the re-spawn dimension's world properties.
        return PlayerHelper.getServerWorld(oldPlayer)
            .getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
    }
}
