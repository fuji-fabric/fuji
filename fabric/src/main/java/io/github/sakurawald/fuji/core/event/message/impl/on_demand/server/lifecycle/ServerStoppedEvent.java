package io.github.sakurawald.fuji.core.event.message.impl.on_demand.server.lifecycle;

import io.github.sakurawald.fuji.core.event.message.impl.on_demand.server.AbstractServerEvent;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public class ServerStoppedEvent extends AbstractServerEvent {
    public ServerStoppedEvent(@NotNull MinecraftServer server) {
        super(server);
    }
}
