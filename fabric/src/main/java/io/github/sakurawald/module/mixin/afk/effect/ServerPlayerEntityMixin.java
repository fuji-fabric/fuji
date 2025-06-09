package io.github.sakurawald.module.mixin.afk.effect;

import com.mojang.authlib.GameProfile;
import io.github.sakurawald.module.initializer.afk.AfkInitializer;
import io.github.sakurawald.module.initializer.afk.accessor.AfkStateAccessor;
import io.github.sakurawald.module.initializer.afk.effect.AfkEffectInitializer;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    @Unique
    final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

    public ServerPlayerEntityMixin(World world, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(world, blockPos, f, gameProfile);
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void invulnerableEffect(ServerWorld serverWorld, DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        if (AfkEffectInitializer.config.model().invulnerable && AfkInitializer.isAfk(player)) {
            cir.setReturnValue(false);
        }
    }

    // note: function move() in 'afk.effect module' will override that one in 'afk module', since the latter Mixin will override the original one.
    @Override
    public void move(MovementType movementType, Vec3d vec3d) {
        AfkStateAccessor afkEx = (AfkStateAccessor) player;

        /* count input on move */
        if (AfkInitializer.isPlayerActuallyMovedItself(movementType, vec3d)) {
            afkEx.fuji$incrInputCounter();
        }

        /* process moveable option */
        if (!AfkEffectInitializer.config.model().moveable && afkEx.fuji$isAfk()) {

            /* store the originalX before the call to move() */
            double originalX = player.getX();
            double originalY = player.getY();
            double originalZ = player.getZ();

            /* if a player moved itself */
            if (movementType == MovementType.PLAYER) {

                if (AfkInitializer.isPlayerActuallyMovedItself(movementType, vec3d)) {
                    // flag the afk state to false, if this movement comes from the player itself.
                    afkEx.fuji$incrInputCounter();

                    // call super to sync the position of player between client and server.
                    super.move(movementType, vec3d);
                }

                // ignore the move() for Vec3d.ZERO
                return;
            }

            // send packet to force set the position of the player in client-side.
            player.requestTeleport(originalX, originalY, originalZ);
            return;
        }

        /* not interested event, call super */
        super.move(movementType, vec3d);
    }

}
