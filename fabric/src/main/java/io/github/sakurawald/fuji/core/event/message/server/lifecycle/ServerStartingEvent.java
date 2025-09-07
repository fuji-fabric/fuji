package io.github.sakurawald.fuji.core.event.message.server.lifecycle;

import io.github.sakurawald.fuji.core.event.message.server.AbstractServerEvent;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public class ServerStartingEvent extends AbstractServerEvent {
    public ServerStartingEvent(@NotNull MinecraftServer server) {
        super(server);
    }
}
