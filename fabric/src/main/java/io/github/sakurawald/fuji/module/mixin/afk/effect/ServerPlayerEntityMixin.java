package io.github.sakurawald.fuji.module.mixin.afk.effect;

import com.google.errorprone.annotations.Keep;
import com.mojang.authlib.GameProfile;
import io.github.sakurawald.fuji.module.initializer.afk.AfkInitializer;
import io.github.sakurawald.fuji.module.initializer.afk.accessor.AfkStateAccessor;
import io.github.sakurawald.fuji.module.initializer.afk.effect.AfkEffectInitializer;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
#if MC_VER > MC_1_21
import net.minecraft.server.world.ServerWorld;
#endif

#if MC_VER < MC_1_21_6
import net.minecraft.util.math.BlockPos;
#endif
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// NOTE: Handle events later to ensure `moveable` option works.
@Mixin(value = ServerPlayerEntity.class, priority = 1000 + 500)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    @Unique
    final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

    /* Constructor for player entity. */
    #if MC_VER < MC_1_21_6
    public ServerPlayerEntityMixin(World world, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(world, blockPos, f, gameProfile);
    }
    #elif MC_VER >= MC_1_21_6
    public ServerPlayerEntityMixin(World world, GameProfile gameProfile) {
        super(world, gameProfile);
    }
    #endif

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void handleInvulnerableEffect(
        #if MC_VER <= MC_1_21
        #elif MC_VER > MC_1_21
            ServerWorld serverWorld,
        #endif
            DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        if (AfkEffectInitializer.config.model().invulnerable
            && AfkInitializer.isAfk(player)) {
            cir.setReturnValue(false);
        }
    }

    // NOTE: function move() in 'afk.effect module' will override that one in 'afk module', since the latter Mixin will override the original one.
    @Override
    @Keep
    public void move(MovementType movementType, Vec3d vec3d) {
        AfkStateAccessor afkEx = (AfkStateAccessor) player;

        /* Count input on move. */
        if (AfkInitializer.isPlayerVelocityNotZero(movementType, vec3d)) {
            AfkInitializer.countAction(player);
        }

        /* Handle moveable option. */
        if (!AfkEffectInitializer.config.model().moveable && AfkInitializer.isAfk(player)) {

            /* Store the originalX before the call to move() */
            double originalX = player.getX();
            double originalY = player.getY();
            double originalZ = player.getZ();

            /* If a player moved itself... */
            if (movementType == MovementType.PLAYER) {

                if (AfkInitializer.isPlayerVelocityNotZero(movementType, vec3d)) {
                    // Flag the afk state to false, if this movement comes from the player itself.
                    AfkInitializer.countAction(player);

                    // Call super to sync the position of player between client and server.
                    super.move(movementType, vec3d);
                }

                // Ignore the move() for Vec3d.ZERO (In Minecraft's protocol, even if the player is still, the client will always send Vec3d.ZERO as current velocity)
                return;
            }

            // Send packet to force set the position of the player in client-side. (If we didn't request a teleport for client, the position of player will de-sync between client and server)
            player.requestTeleport(originalX, originalY, originalZ);
            return;
        }

        /* Not interested event, call super to process the default logic. */
        super.move(movementType, vec3d);
    }

}
