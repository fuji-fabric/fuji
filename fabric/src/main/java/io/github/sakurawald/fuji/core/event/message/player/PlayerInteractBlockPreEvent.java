package io.github.sakurawald.fuji.core.event.message.player;

import io.github.sakurawald.fuji.core.event.message.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlayerInteractBlockPreEvent extends BaseEvent {
    @NotNull ServerPlayerEntity player;
    @NotNull World world;
    @NotNull ItemStack itemStack;
    @NotNull Hand hand;
    // NOTE: For door blocks, the block hit result is exactly the block pos that is hit.
    @NotNull BlockHitResult blockHitResult;
    @NotNull CallbackInfoReturnable<ActionResult> callbackInfoReturnable;
}
