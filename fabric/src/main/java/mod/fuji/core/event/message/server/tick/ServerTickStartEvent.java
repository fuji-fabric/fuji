package mod.fuji.core.event.message.server.tick;


import mod.fuji.core.event.message.server.AbstractServerEvent;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

/**
 * Fired before the call to tick worlds.
 **/
public class ServerTickStartEvent extends AbstractServerEvent {

    public ServerTickStartEvent(@NotNull MinecraftServer server) {
        super(server);
    }

}
