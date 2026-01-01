package mod.fuji.module.mixin.core.event;

import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.player.PlayerInteractEntityPreEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@PhasedMixinTemplate
@Mixin(Entity.class)
public class PlayerInteractEntityPreEventMixin {

    @EventProducer(PlayerInteractEntityPreEvent.class)
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    void producePlayerInteractEntityPreEvent(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        PlayerHelper.Kind.ifServerPlayerEntity(player, serverPlayer -> {
            Entity entity = (Entity) (Object) this;
            PlayerInteractEntityPreEvent event = new PlayerInteractEntityPreEvent(serverPlayer, entity, hand, cir);
            EventManager.dispatchEvent(PlayerInteractEntityPreEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
        });
    }

}
