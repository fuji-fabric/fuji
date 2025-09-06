package io.github.sakurawald.fuji.module.mixin.core.event.on_demand;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.sakurawald.annotation.PhasedMixinTemplate;
import io.github.sakurawald.auxiliary.WeaverUtil;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.impl.on_demand.ModifyPlayerDisplayNameEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@PhasedMixinTemplate
@Mixin(value = PlayerEntity.class)
public class ModifyPlayerDisplayNameEventMixin {

    @Unique
    @NotNull
    final PlayerEntity player = (PlayerEntity) (Object) this;

    @EventProducer(ModifyPlayerDisplayNameEvent.class)
    @ModifyReturnValue(method = "getDisplayName", at = @At("RETURN"))
    public Text produceModifyPlayerDisplayNameEvent(@Nullable Text original) {
        ModifyPlayerDisplayNameEvent event = new ModifyPlayerDisplayNameEvent(player, original);
        EventManager.dispatchEvent(ModifyPlayerDisplayNameEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
        return event.getText();
    }

}
