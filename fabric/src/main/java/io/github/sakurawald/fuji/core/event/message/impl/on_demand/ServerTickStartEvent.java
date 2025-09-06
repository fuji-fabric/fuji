package io.github.sakurawald.fuji.core.event.message.impl.on_demand;


import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

@ForDeveloper("Fired before the call to tick worlds.")
public class ServerTickStartEvent extends AbstractServerEvent {

    public ServerTickStartEvent(@NotNull MinecraftServer server) {
        super(server);
    }

}
