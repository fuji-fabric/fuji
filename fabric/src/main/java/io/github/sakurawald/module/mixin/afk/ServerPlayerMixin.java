package io.github.sakurawald.module.mixin.afk;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.executor.CommandExecutor;
import io.github.sakurawald.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.module.initializer.afk.AfkInitializer;
import io.github.sakurawald.module.initializer.afk.accessor.AfkStateAccessor;
import io.github.sakurawald.module.initializer.afk.config.model.AfkConfigModel;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

// to override tab list name in `tab list module`
@Mixin(value = ServerPlayerEntity.class, priority = 1000 - 250)
public abstract class ServerPlayerMixin extends PlayerEntity implements AfkStateAccessor {

    @Unique
    private final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

    /* props for afk state */
    @Unique
    private boolean afk;

    @Unique
    private long inputCounter = 0;

    public ServerPlayerMixin(World world, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(world, blockPos, f, gameProfile);
    }

    @ModifyReturnValue(method = "getPlayerListName", at = @At("RETURN"))
    public Text $getPlayerListName(Text original) {
        if (fuji$isAfk()) {
            return TextHelper.getTextByValue(player, AfkInitializer.config.model().format);
        }

        return original;
    }

    @Inject(method = "updateLastActionTime", at = @At("TAIL"))
    public void $updateLastActionTime(CallbackInfo ci) {
        this.fuji$incrInputCounter();
    }

    @Override
    public void fuji$changeAfk(boolean flag) {
        // change
        this.afk = flag;

        // update tab list name
        ServerHelper.sendPacketToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, (ServerPlayerEntity) (Object) this));

        // trigger event
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

        if (fuji$isAfk()) {
            fuji$changeAfk(false);
        }
    }

    @Override
    public long fuji$getInputCounter() {
        return this.inputCounter;
    }

    @Override
    public void move(MovementType movementType, Vec3d vec3d) {
        /* count input on move */
        if (AfkInitializer.isPlayerActuallyMovedItself(movementType, vec3d)) {
            fuji$incrInputCounter();
        }

        /* not interested event, call super */
        super.move(movementType, vec3d);
    }
}
