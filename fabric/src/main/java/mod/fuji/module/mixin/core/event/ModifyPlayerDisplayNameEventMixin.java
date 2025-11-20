package mod.fuji.module.mixin.core.event;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.player.ModifyPlayerDisplayNameEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@PhasedMixinTemplate
@Mixin(value = Player.class)
public class ModifyPlayerDisplayNameEventMixin {

    @Unique
    @NotNull
    final Player player = (Player) (Object) this;

    @EventProducer(ModifyPlayerDisplayNameEvent.class)
    @ModifyReturnValue(method = "getDisplayName", at = @At("RETURN"))
    public Component produceModifyPlayerDisplayNameEvent(@Nullable Component original) {
        ModifyPlayerDisplayNameEvent event = new ModifyPlayerDisplayNameEvent(player, original);
        EventManager.dispatchEvent(ModifyPlayerDisplayNameEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
        return event.getText();
    }

}
