package io.github.sakurawald.fuji.core.event.message.player;

import io.github.sakurawald.fuji.core.event.message.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PlayerChatMessageSentEvent extends BaseEvent {

    SignedMessage signedMessage;
    MessageType.Parameters parameters;

}
