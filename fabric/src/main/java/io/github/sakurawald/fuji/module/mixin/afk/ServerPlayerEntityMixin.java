package io.github.sakurawald.fuji.module.mixin.afk;

import com.google.errorprone.annotations.Keep;
import com.mojang.authlib.GameProfile;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PacketHelper;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.module.initializer.afk.AfkInitializer;
import io.github.sakurawald.fuji.module.initializer.afk.accessor.AfkStateAccessor;
import io.github.sakurawald.fuji.module.initializer.afk.config.model.AfkConfigModel;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
#if MC_VER < MC_1_21_6
import net.minecraft.util.math.BlockPos;
#endif
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements AfkStateAccessor {

    @Unique
    private final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

    /* Variables for afk state */
    @Unique
    private boolean afk;

    @Unique
    private long inputCounter = 0;

    /* Constructor for player. */
    #if MC_VER < MC_1_21_6
    public ServerPlayerEntityMixin(World world, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(world, blockPos, f, gameProfile);
    }
    #elif MC_VER >= MC_1_21_6
    public ServerPlayerEntityMixin(World world, GameProfile gameProfile) {
        super(world, gameProfile);
    }
    #endif

    @Inject(method = "updateLastActionTime", at = @At("TAIL"))
    public void $updateLastActionTime(CallbackInfo ci) {
        AfkInitializer.countAction(player);
    }

    @Override
    public void fuji$changeAfk(boolean flag) {
        // Change afk flag.
        this.afk = flag;

        // Update tab list name.
        PacketHelper.sendPacketToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, (ServerPlayerEntity) (Object) this));

        // Trigger afk events.
        AfkConfigModel.AfkEvent afkEvent = AfkInitializer.config.model().afk_event;
        List<String> commandList = this.afk ? afkEvent.on_enter_afk : afkEvent.on_leave_afk;
        CommandExecutor.execute(ExtendedCommandSource.asConsole(player.getCommandSource()), commandList);
    }

    @Override
    public boolean fuji$isAfk() {
        return this.afk;
    }

    @Override
    public void fuji$incrInputCounter() {
        this.inputCounter++;

        /* Set afk flag to false, once receive any input action. */
        if (fuji$isAfk()) {
            fuji$changeAfk(false);
        }
    }

    @Override
    public long fuji$getInputCounter() {
        return this.inputCounter;
    }


    @TestCase(action = "Try to move a player in afk state.", targets = "The `moveable` option should work.")
    // NOTE: Here we override the original move() function, we use @Override since we can't inject into a super method.
    @Keep
    @Override
    public void move(MovementType movementType, Vec3d vec3d) {
        /* Count input on move. */
        if (AfkInitializer.isPlayerVelocityNotZero(movementType, vec3d)) {
            AfkInitializer.countAction(player);
        }

        /* Not interested event, call super to handle the default logic. */
        super.move(movementType, vec3d);
    }
}
