package mod.fuji.core.event.message.player;

import mod.fuji.core.event.message.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlayerChatMessagePreEvent extends BaseEvent {

    @NotNull ServerPlayer player;
    @NotNull PlayerChatMessage signedMessage;
    @NotNull ChatType.Bound parameters;
}
