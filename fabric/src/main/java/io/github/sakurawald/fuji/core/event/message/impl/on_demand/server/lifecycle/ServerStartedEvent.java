package io.github.sakurawald.fuji.core.event.message.impl.on_demand.server.lifecycle;

import io.github.sakurawald.fuji.core.event.message.impl.on_demand.server.AbstractServerEvent;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public class ServerStartedEvent extends AbstractServerEvent {
    public ServerStartedEvent(@NotNull MinecraftServer server) {
        super(server);
    }
}
