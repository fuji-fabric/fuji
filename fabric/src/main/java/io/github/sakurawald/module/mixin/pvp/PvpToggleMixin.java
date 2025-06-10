package io.github.sakurawald.module.mixin.pvp;

import com.mojang.authlib.GameProfile;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.module.initializer.pvp.PvpInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
#if MC_VER < MC_1_21_6
import net.minecraft.util.math.BlockPos;
#endif
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class PvpToggleMixin extends PlayerEntity {

    #if MC_VER < MC_1_21_6
    public PvpToggleMixin(World world, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(world, blockPos, f, gameProfile);
    }
    #elif MC_VER >= MC_1_21_6
    public PvpToggleMixin(World world, GameProfile gameProfile) {
        super(world, gameProfile);
    }
    #endif

    @Inject(method = "shouldDamagePlayer", at = @At("HEAD"), cancellable = true)
    public void $shouldDamagePlayer(@NotNull PlayerEntity sourcePlayer, @NotNull CallbackInfoReturnable<Boolean> cir) {
        if (this == sourcePlayer) return;

        ServerPlayerEntity player = CommandHelper.getCommandSource(sourcePlayer).getPlayer();

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
