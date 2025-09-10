package io.github.sakurawald.fuji.module.mixin.core.event;

import io.github.sakurawald.annotation.PhasedMixinTemplate;
import io.github.sakurawald.auxiliary.WeaverUtil;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.message.player.PlayerInteractBlockPreEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@PhasedMixinTemplate
@Mixin(ServerPlayerInteractionManager.class)
public class PlayerInteractBlockPreEventMixin {

    @Shadow
    protected ServerWorld world;

    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @EventProducer(PlayerInteractBlockPreEvent.class)
    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    void producePlayerInteractBlockPreEvent(ServerPlayerEntity serverPlayerEntity, @NotNull World world, ItemStack itemStack, Hand hand, @NotNull BlockHitResult blockHitResult, @NotNull CallbackInfoReturnable<ActionResult> cir) {
        PlayerInteractBlockPreEvent event = new PlayerInteractBlockPreEvent(player, world, itemStack, hand, blockHitResult, cir);
        EventManager.dispatchEvent(PlayerInteractBlockPreEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }

}
