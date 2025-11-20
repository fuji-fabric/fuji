package mod.fuji.core.event.message.player;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class PlayerJoinedEvent extends AbstractPlayerEvent{
    public PlayerJoinedEvent(@NotNull ServerPlayer player) {
        super(player);
    }
}
