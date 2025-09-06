package io.github.sakurawald.fuji.core.event.impl;

import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.event.abst.SimpleEvent;
import net.minecraft.server.MinecraftServer;

public class ServerTickEvents {

    @ForDeveloper("Fired before the call to tick worlds.")
    public static final SimpleEvent<StartServerTickCallback> START_SERVER_TICK = new SimpleEvent<>((listeners) -> (server) -> listeners.forEach(listener -> listener.fire(server)));

    public interface StartServerTickCallback {
        void fire(MinecraftServer server);
    }

}
