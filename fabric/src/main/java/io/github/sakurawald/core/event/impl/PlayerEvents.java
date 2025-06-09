package io.github.sakurawald.core.event.impl;

import io.github.sakurawald.core.event.abst.Event;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerEvents {

    public static final Event<PlayerOnDamagedCallback> ON_DAMAGED = new Event<>((listeners) -> (p, s, a) -> {
        for (PlayerOnDamagedCallback listener : listeners) {
            listener.fire(p, s, a);
        }
    });


    public interface PlayerOnDamagedCallback {
        void fire(ServerPlayerEntity player, DamageSource damageSource, float amount);
    }
}
