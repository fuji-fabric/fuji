package mod.fuji.core.event.message.player;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class PlayerLeftEvent extends AbstractPlayerEvent{
    public PlayerLeftEvent(@NotNull ServerPlayerEntity player) {
        super(player);
    }
}
