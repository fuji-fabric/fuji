package mod.fuji.module.mixin.core.event;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.player.ModifyPlayerDisplayNameEvent;
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
