package mod.fuji.core.event.message.server.tick;


import mod.fuji.core.event.message.server.AbstractServerEvent;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public class ServerTickEndEvent extends AbstractServerEvent {

    public ServerTickEndEvent(@NotNull MinecraftServer server) {
        super(server);
    }

}
