package io.github.sakurawald.fuji.core.event.message.impl.on_demand.player;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class OnPlayerLeftEvent extends AbstractPlayerEvent{
    public OnPlayerLeftEvent(@NotNull ServerPlayerEntity player) {
        super(player);
    }
}
