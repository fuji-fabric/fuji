package io.github.sakurawald.core.event.impl;

import io.github.sakurawald.core.event.abst.Event;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public class ServerWorldEvents {

    public static final Event<ServerWorldUnloadCallback> UNLOAD = new Event<>((listeners) -> (server, world) -> {
        for (ServerWorldUnloadCallback listener : listeners) {
            listener.fire(server, world);
        }
    });
    public static final Event<ServerWorldLoadCallback> LOAD = new Event<>((listeners) -> (server, world) -> {
        for (ServerWorldLoadCallback listener : listeners) {
            listener.fire(server, world);
        }
    });

    public interface ServerWorldUnloadCallback {
        void fire(MinecraftServer server, ServerWorld world);
    }

    public interface ServerWorldLoadCallback {
        void fire(MinecraftServer server, ServerWorld world);
    }
}
