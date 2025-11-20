package mod.fuji.module.mixin.core.event;

import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.player.PlayerInteractItemPreEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
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
public class PlayerInteractItemPreEventMixin {

    @Shadow
    protected ServerLevel level;

    @Shadow
    @Final
    protected ServerPlayer player;

    @EventProducer(PlayerInteractItemPreEvent.class)
    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    void producePlayerInteractItemPreEvent(ServerPlayer serverPlayerEntity, Level world, @NotNull ItemStack itemStack, InteractionHand hand, @NotNull CallbackInfoReturnable<InteractionResult> cir) {
        PlayerInteractItemPreEvent event = new PlayerInteractItemPreEvent(player, world, itemStack, hand, cir);
        EventManager.dispatchEvent(PlayerInteractItemPreEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }
}
