package io.github.sakurawald.fuji.core.event.message.player;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class OnPlayerDamagedEvent extends AbstractPlayerEvent{

    public OnPlayerDamagedEvent(@NotNull ServerPlayerEntity player) {
        super(player);
    }
}
