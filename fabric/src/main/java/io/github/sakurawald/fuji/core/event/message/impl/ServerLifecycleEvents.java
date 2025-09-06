package io.github.sakurawald.fuji.core.event.message.impl;

import io.github.sakurawald.fuji.core.event.message.abst.SimpleEvent;
import net.minecraft.server.MinecraftServer;

public class ServerLifecycleEvents {

    public static SimpleEvent<ServerStartingCallback> SERVER_STARTING = new SimpleEvent<>((listeners) -> (server) -> listeners.forEach(listener -> listener.fire(server)));

    public static SimpleEvent<ServerStartedCallback> SERVER_STARTED = new SimpleEvent<>((listeners) -> (server) -> listeners.forEach(listener -> listener.fire(server)));

    public static SimpleEvent<ServerStoppedCallback> SERVER_STOPPED = new SimpleEvent<>((listeners) -> (server) -> listeners.forEach(listener -> listener.fire(server)));

    public static SimpleEvent<ServerStoppingCallback> SERVER_STOPPING = new SimpleEvent<>((listeners) -> (server) -> listeners.forEach(listener -> listener.fire(server)));

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
