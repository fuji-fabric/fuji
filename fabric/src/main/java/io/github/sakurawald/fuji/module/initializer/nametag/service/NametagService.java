package io.github.sakurawald.fuji.module.initializer.nametag.service;

import io.github.sakurawald.fuji.core.annotation.Unused;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.player.PlayerTeleportPreEvent;
import io.github.sakurawald.fuji.core.event.message.player.PlayerWorldChangedEvent;
import io.github.sakurawald.fuji.core.event.message.server.tick.ServerTickEndEvent;
import io.github.sakurawald.fuji.module.initializer.nametag.structure.NametagEntity;
import io.github.sakurawald.fuji.module.initializer.nametag.structure.NametagEntitySyncer;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class NametagService {
    public static Map<ServerPlayerEntity, NametagEntity> nametagEntityMap = new ConcurrentHashMap<>();

    private static @NotNull NametagEntity setupNametagEntity(@NotNull ServerPlayerEntity player) {
        /* Make the nametag entity. */
        NametagEntity nametagEntity = NametagEntity.make(player);

        /* Sync the nametag entity to client world. */
        NametagEntitySyncer.syncNametagEntityToClientWorld(nametagEntity);
        return nametagEntity;
    }

    public static void processNametagsForOnlinePlayers() {
        /* Remove invalid nametag entities. */
        nametagEntityMap.values().removeIf(NametagEntity::shouldRemove);

        /* Update the nametag entities. */
        PlayerHelper.Lookup.getOnlinePlayers().forEach(player -> {
            // Skip making the nametag entity for the player, if a discard reason is present.
            if (NametagEntity.getNametagDiscardReason(player).isPresent()) return;

            // Make the nametag if not exists.
            NametagEntity nametagEntity = nametagEntityMap.computeIfAbsent(player, key -> setupNametagEntity(player));

            // Render the nametag.
            nametagEntity.update();
        });
    }

    @EventConsumer
    private static void tickNametagEntities(@Unused ServerTickEndEvent event) {
        nametagEntityMap.values().forEach(NametagEntity::tick);
    }

    private static void removeNametagEntity(ServerPlayerEntity player) {
        Optional
            .ofNullable(nametagEntityMap.get(player))
            .ifPresent(NametagEntity::setRemoved);
    }

    @EventConsumer(injectorPriority = EventConsumer.HIGHEST)
    private static void consumePlayerTeleportPreEvent(PlayerTeleportPreEvent event) {
        if (event.getCallbackInfo().isCancelled()) return;
        removeNametagEntity(event.getPlayer());
    }

    @EventConsumer
    private static void consumePlayerWorldChangedEvent(PlayerWorldChangedEvent event) {
        removeNametagEntity(event.getPlayer());
    }

    public static void removeAllNametagEntities() {
        nametagEntityMap.values().forEach(NametagEntity::setRemoved);
    }
}
