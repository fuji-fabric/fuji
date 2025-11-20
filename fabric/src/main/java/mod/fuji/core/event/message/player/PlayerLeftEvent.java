package mod.fuji.core.event.message.player;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class PlayerLeftEvent extends AbstractPlayerEvent{
    public PlayerLeftEvent(@NotNull ServerPlayer player) {
        super(player);
    }
}
