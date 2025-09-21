package mod.fuji.module.mixin.world.gamerule;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @ModifyExpressionValue(method = "copyFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    boolean useOriginalPlayerGameRules(boolean original, @SuppressWarnings("UnresolvedLocalCapture") @Local(argsOnly = true) ServerPlayerEntity oldPlayer) {
        // NOTE: For `keepInventory` game rule. Its value is checked in copyFrom() method after the player is dead. The value comes from the re-spawn dimension's world properties.
        return PlayerHelper.getServerWorld(oldPlayer)
            .getGameRules().getBoolean(GameRules.KEEP_INVENTORY);
    }
}
