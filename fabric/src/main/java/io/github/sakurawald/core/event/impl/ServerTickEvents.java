package io.github.sakurawald.core.event.impl;

import io.github.sakurawald.core.event.abst.Event;
import net.minecraft.server.MinecraftServer;

public class ServerTickEvents {

    public static final Event<StartServerTickCallback> START_SERVER_TICK = new Event<>((listeners) -> (server) -> {
        for (StartServerTickCallback listener : listeners) {
            listener.fire(server);
        }
    });

    public interface StartServerTickCallback {
        void fire(MinecraftServer server);
    }

}
