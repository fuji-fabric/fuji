package mod.fuji.core.event.message.player;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class PlayerJoinedEvent extends AbstractPlayerEvent{
    public PlayerJoinedEvent(@NotNull ServerPlayerEntity player) {
        super(player);
    }
}
