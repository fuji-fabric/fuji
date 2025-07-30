package io.github.sakurawald.fuji.module.mixin.pvp;

import com.mojang.authlib.GameProfile;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.pvp.PvpInitializer;
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
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    #if MC_VER < MC_1_21_6
    public ServerPlayerEntityMixin(World world, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(world, blockPos, f, gameProfile);
    }
    #elif MC_VER >= MC_1_21_6
    public ServerPlayerEntityMixin(World world, GameProfile gameProfile) {
        super(world, gameProfile);
    }
    #endif

    @Inject(method = "shouldDamagePlayer", at = @At("HEAD"), cancellable = true)
    public void shouldBeDamagedByAnotherPlayer(@NotNull PlayerEntity playerEntity, @NotNull CallbackInfoReturnable<Boolean> cir) {
        /* Don't flint a TNT to kill yourself. */
        if (playerEntity.equals(this)) return;

        /* Okay, the damage source player should enable pvp first. */
        ServerPlayerEntity damageSourcePlayer = (ServerPlayerEntity) playerEntity;
        if (!PvpInitializer.isPvpEnabled(PlayerHelper.getPlayerName(damageSourcePlayer))) {
            TextHelper.sendTextByKey(damageSourcePlayer, "pvp.check.off.me");
            cir.setReturnValue(false);
            return;
        }

        /* Then, the damage target player should enable pvp. */
        String myName = PlayerHelper.getPlayerName(this);
        if (!PvpInitializer.isPvpEnabled(myName)) {
            TextHelper.sendTextByKey(damageSourcePlayer, "pvp.check.off.others", myName);
            cir.setReturnValue(false);
        }
    }
}
