package io.github.sakurawald.fuji.core.event.message.impl.on_demand;

import io.github.sakurawald.fuji.core.event.message.abst.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class OnPlayerChatMessageEvent extends BaseEvent {

    @NotNull ServerPlayerEntity player;
    @NotNull SignedMessage signedMessage;
    @NotNull MessageType.Parameters parameters;
}
