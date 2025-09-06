package io.github.sakurawald.fuji.core.event.message.impl;

import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.event.message.abst.SimpleEvent;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerEvents {

    @ForDeveloper("""
        The monitor event, if the `damage` actually happened.
        """)
    public static final SimpleEvent<PlayerOnDamagedCallback> ON_DAMAGED = new SimpleEvent<>((listeners) -> (p, s, a) -> listeners.forEach(listener -> listener.fire(p, s, a)));
    public interface PlayerOnDamagedCallback {
        void fire(ServerPlayerEntity player, DamageSource damageSource, float amount);
    }

}
