package mod.fuji.module.initializer.nametag.service;

import mod.fuji.core.annotation.Unused;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.PlayerTeleportPreEvent;
import mod.fuji.core.event.message.player.PlayerWorldChangedEvent;
import mod.fuji.core.event.message.server.tick.ServerTickEndEvent;
import mod.fuji.module.initializer.nametag.NametagInitializer;
import mod.fuji.module.initializer.nametag.structure.NametagEntity;
import mod.fuji.module.initializer.nametag.structure.NametagEntitySyncer;
import mod.fuji.module.initializer.nametag.structure.NametagPlayerPreferences;
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

        /* Update the mapping as soon as possible. */
        nametagEntityMap.put(player, nametagEntity);

        /* Sync the nametag entity to client world. */
        NametagEntitySyncer.syncNametagEntityToClientWorld(nametagEntity);
        return nametagEntity;
    }

    public static void processNametagEntities() {
        /* Remove invalid nametag entities. */
        nametagEntityMap.values().removeIf(NametagEntity::shouldRemove);

        /* Update the nametag entities. */
        PlayerHelper.Lookup
            .getOnlinePlayers()
            .forEach(NametagService::processNametagEntity);
    }

    private static void processNametagEntity(@NotNull ServerPlayerEntity player) {
        // Skip making the nametag entity for the player, if a discard reason is present.
        if (getNametagEntityRemovedReason(player).isPresent()) return;

        // Make the nametag if not exists.
        NametagEntity nametagEntity = Optional
            .ofNullable(nametagEntityMap.get(player))
            .orElseGet(() -> setupNametagEntity(player));

        // Render the nametag.
        nametagEntity.updateTrackedData();
    }

    public static Optional<NametagEntity> getNametagEntity(@NotNull ServerPlayerEntity player) {
        return Optional.ofNullable(nametagEntityMap.get(player));
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

    public static @NotNull NametagPlayerPreferences getOrCreateNametagPlayerPreferences(@NotNull ServerPlayerEntity player) {
        String playerName = PlayerHelper.getPlayerName(player);
        return NametagInitializer.data.model()
            .getPreferences()
            .computeIfAbsent(playerName, key -> new NametagPlayerPreferences());
    }

    @SuppressWarnings("RedundantIfStatement")
    public static boolean shouldRenderNametagEntity(@NotNull NametagEntity nametagEntity) {
        ServerPlayerEntity ownerPlayer = nametagEntity.getOwnerPlayer();
        if (ownerPlayer.isSneaking()) return false;
        if (ownerPlayer.isInvisible()) return false;

        return true;
    }

    public static Optional<String> getNametagEntityRemovedReason(@NotNull ServerPlayerEntity ownerPlayer) {
        if (ownerPlayer.isDead()) return Optional.of("The entity is dead.");

        // NOTE: when the player jumps into the ender portal in the end, its world is minecraft:overworld, its removal reason is `CHANGED_DIMENSION`
        if (ownerPlayer.getRemovalReason() != null) return Optional.of("The entity is removed.");
        if (!getOrCreateNametagPlayerPreferences(ownerPlayer).isEnableNametagEntity()) return Optional.of("The player has turn off the nametag entity.");

        return Optional.empty();
    }
}
