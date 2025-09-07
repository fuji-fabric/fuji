package io.github.sakurawald.fuji.core.event.message.server.tick;


import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.event.message.server.AbstractServerEvent;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

@ForDeveloper("Fired before the call to tick worlds.")
public class ServerTickStartEvent extends AbstractServerEvent {

    public ServerTickStartEvent(@NotNull MinecraftServer server) {
        super(server);
    }

}
