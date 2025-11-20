package mod.fuji.module.mixin.core.event;

import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.player.PlayerInteractBlockPreEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@PhasedMixinTemplate
@Mixin(ServerPlayerGameMode.class)
public class PlayerInteractBlockPreEventMixin {

    @Shadow
    protected ServerLevel level;

    @Shadow
    @Final
    protected ServerPlayer player;

    @EventProducer(PlayerInteractBlockPreEvent.class)
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    void producePlayerInteractBlockPreEvent(ServerPlayer serverPlayerEntity, @NotNull Level world, ItemStack itemStack, InteractionHand hand, @NotNull BlockHitResult blockHitResult, @NotNull CallbackInfoReturnable<InteractionResult> cir) {
        PlayerInteractBlockPreEvent event = new PlayerInteractBlockPreEvent(player, world, itemStack, hand, blockHitResult, cir);
        EventManager.dispatchEvent(PlayerInteractBlockPreEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }

}
