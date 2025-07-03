package io.github.sakurawald.fuji.core.event.impl;

import io.github.sakurawald.fuji.core.event.abst.Event;
import net.minecraft.server.MinecraftServer;

public class ServerLifecycleEvents {

    public static Event<ServerStartingCallback> SERVER_STARTING = new Event<>((listeners) -> (server) -> listeners.forEach(listener -> listener.fire(server)));

    public static Event<ServerStartedCallback> SERVER_STARTED = new Event<>((listeners) -> (server) -> listeners.forEach(listener -> listener.fire(server)));

    public static Event<ServerStoppedCallback> SERVER_STOPPED = new Event<>((listeners) -> (server) -> listeners.forEach(listener -> listener.fire(server)));

    public static Event<ServerStoppingCallback> SERVER_STOPPING = new Event<>((listeners) -> (server) -> listeners.forEach(listener -> listener.fire(server)));

    public interface ServerStartingCallback {
        void fire(MinecraftServer server);
    }

    public interface ServerStartedCallback {
        void fire(MinecraftServer server);
    }

    public interface ServerStoppedCallback {
        void fire(MinecraftServer server);
    }

    public interface ServerStoppingCallback {
        void fire(MinecraftServer server);
    }
}
