package mod.fuji.core.event.message.player;

import mod.fuji.core.event.message.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlayerInteractEntityPreEvent extends BaseEvent {
    @NotNull ServerPlayer player;
    @NotNull Entity entity;
    @NotNull InteractionHand hand;
    @NotNull CallbackInfoReturnable<InteractionResult> callbackInfoReturnable;
}
