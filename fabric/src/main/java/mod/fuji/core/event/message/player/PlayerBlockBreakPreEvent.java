package mod.fuji.core.event.message.player;

import mod.fuji.core.event.message.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlayerBlockBreakPreEvent extends BaseEvent {
    @NotNull ServerPlayerEntity player;
    @NotNull ServerWorld world;
    @NotNull BlockPos blockPos;
    @NotNull CallbackInfoReturnable<Boolean> callbackInfoReturnable;
}
