package io.github.sakurawald.fuji.core.event.message.server.tick;


import io.github.sakurawald.fuji.core.event.message.server.AbstractServerEvent;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public class ServerTickEndEvent extends AbstractServerEvent {

    public ServerTickEndEvent(@NotNull MinecraftServer server) {
        super(server);
    }

}
