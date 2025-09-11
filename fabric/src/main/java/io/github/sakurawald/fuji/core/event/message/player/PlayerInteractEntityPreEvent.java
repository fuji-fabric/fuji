package io.github.sakurawald.fuji.core.event.message.player;

import io.github.sakurawald.fuji.core.event.message.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlayerInteractEntityPreEvent extends BaseEvent {
    @NotNull ServerPlayerEntity player;
    @NotNull Entity entity;
    @NotNull Hand hand;
    @NotNull CallbackInfoReturnable<ActionResult> callbackInfoReturnable;
}
