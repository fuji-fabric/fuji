package mod.fuji.core.event.message.player;

import mod.fuji.core.event.message.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PlayerChatMessageSentEvent extends BaseEvent {

    ServerPlayer receiverPlayer;
    PlayerChatMessage signedMessage;
    ChatType.Bound parameters;

}
