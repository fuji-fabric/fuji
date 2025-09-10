package io.github.sakurawald.fuji.module.mixin.core.event;


import io.github.sakurawald.annotation.PhasedMixinTemplate;
import io.github.sakurawald.auxiliary.WeaverUtil;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.message.player.PlayerBlockBreakPreEvent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@PhasedMixinTemplate
@Mixin(ServerPlayerInteractionManager.class)
public class PlayerBlockBreakPreEventMixin {

    @Shadow
    protected ServerWorld world;

    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @EventProducer(PlayerBlockBreakPreEvent.class)
    @Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
    void producePlayerBlockBreakPreEvent(BlockPos blockPos, @NotNull CallbackInfoReturnable<Boolean> cir) {
        PlayerBlockBreakPreEvent event = new PlayerBlockBreakPreEvent(player, world, blockPos, cir);
        EventManager.dispatchEvent(PlayerBlockBreakPreEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }
}
