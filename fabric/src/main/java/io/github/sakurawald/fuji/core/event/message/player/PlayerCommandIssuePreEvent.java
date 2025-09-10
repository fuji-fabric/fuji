package io.github.sakurawald.fuji.core.event.message.player;

import io.github.sakurawald.fuji.core.event.message.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlayerCommandIssuePreEvent extends BaseEvent {
    @NotNull ServerPlayerEntity player;
    @NotNull String commandString;
    @NotNull CallbackInfo callbackInfo;
}
