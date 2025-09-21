package mod.fuji.core.event.message.player;

import mod.fuji.core.event.message.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlayerActionEvent extends BaseEvent {
    @NotNull ServerPlayerEntity player;
    @NotNull PlayerActionC2SPacket packet;
    @NotNull CallbackInfo callbackInfo;
}
