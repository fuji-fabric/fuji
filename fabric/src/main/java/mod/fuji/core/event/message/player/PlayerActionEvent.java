package mod.fuji.core.event.message.player;

import mod.fuji.core.event.message.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlayerActionEvent extends BaseEvent {
    @NotNull ServerPlayer player;
    @NotNull ServerboundPlayerActionPacket packet;
    @NotNull CallbackInfo callbackInfo;
}
