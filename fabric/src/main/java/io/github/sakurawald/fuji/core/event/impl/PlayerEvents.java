package io.github.sakurawald.fuji.core.event.impl;

import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.event.abst.Event;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerEvents {

    @ForDeveloper("""
        The monitor event, if the `damage` actually happened.
        """)
    public static final Event<PlayerOnDamagedCallback> ON_DAMAGED = new Event<>((listeners) -> (p, s, a) -> listeners.forEach(listener -> listener.fire(p, s, a)));

    public interface PlayerOnDamagedCallback {
        void fire(ServerPlayerEntity player, DamageSource damageSource, float amount);
    }

    public static final Event<PlayerJoinedEvent> ON_PLAYER_JOINED = new Event<>((listeners) -> (p) -> listeners.forEach(listener -> listener.fire(p)));

    public interface PlayerJoinedEvent {
        void fire(ServerPlayerEntity player);
    }
}
