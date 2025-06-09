package io.github.sakurawald.module.mixin.pvp;

import com.mojang.authlib.GameProfile;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.module.initializer.pvp.PvpInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class PvpToggleMixin extends PlayerEntity {

    public PvpToggleMixin(@NotNull World world, @NotNull BlockPos pos, float yaw, @NotNull GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "shouldDamagePlayer", at = @At("HEAD"), cancellable = true)
    public void $shouldDamagePlayer(@NotNull PlayerEntity sourcePlayer, @NotNull CallbackInfoReturnable<Boolean> cir) {
        if (this == sourcePlayer) return;

        ServerPlayerEntity player = sourcePlayer.getCommandSource((ServerWorld) sourcePlayer.getWorld()).getPlayer();
        if (player == null) return;

        if (!PvpInitializer.contains(sourcePlayer.getGameProfile().getName())) {
            TextHelper.sendMessageByKey(player, "pvp.check.off.me");
            cir.setReturnValue(false);
            return;
        }

        if (!PvpInitializer.contains(this.getGameProfile().getName())) {
            TextHelper.sendMessageByKey(player, "pvp.check.off.others", this.getGameProfile().getName());
            cir.setReturnValue(false);
        }
    }
}
