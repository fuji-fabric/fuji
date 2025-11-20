package mod.fuji.module.mixin.core.event;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.player.ModifyPlayerListNameEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@PhasedMixinTemplate
@Mixin(value = ServerPlayer.class)
public abstract class ModifyPlayerListNameEventMixin {

    @Unique
    private final ServerPlayer player = (ServerPlayer) (Object) this;

    @EventProducer(ModifyPlayerListNameEvent.class)
    @ModifyReturnValue(method = "getTabListDisplayName", at = @At("RETURN"))
    public Component produceModifyPlayerListNameEvent(@Nullable Component original) {
        ModifyPlayerListNameEvent event = new ModifyPlayerListNameEvent(player, original);
        EventManager.dispatchEvent(ModifyPlayerListNameEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
        return event.getText();
    }

}
