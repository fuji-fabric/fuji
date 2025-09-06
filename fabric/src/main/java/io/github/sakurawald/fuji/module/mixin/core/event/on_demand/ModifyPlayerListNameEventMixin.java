package io.github.sakurawald.fuji.module.mixin.core.event.on_demand;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.sakurawald.annotation.PhasedMixinTemplate;
import io.github.sakurawald.auxiliary.WeaverUtil;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.message.impl.on_demand.player.ModifyPlayerListNameEvent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@PhasedMixinTemplate
@Mixin(value = ServerPlayerEntity.class)
public abstract class ModifyPlayerListNameEventMixin {

    @Unique
    private final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

    @EventProducer(ModifyPlayerListNameEvent.class)
    @ModifyReturnValue(method = "getPlayerListName", at = @At("RETURN"))
    public Text produceModifyPlayerListNameEvent(@Nullable Text original) {
        ModifyPlayerListNameEvent event = new ModifyPlayerListNameEvent(player, original);
        EventManager.dispatchEvent(ModifyPlayerListNameEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
        return event.getText();
    }

}
