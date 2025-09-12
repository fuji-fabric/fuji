package io.github.sakurawald.fuji.module.initializer.nametag;

import io.github.sakurawald.fuji.core.annotation.Unused;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.player.PlayerTeleportPreEvent;
import io.github.sakurawald.fuji.core.event.message.server.tick.ServerTickEndEvent;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.nametag.config.model.NametagConfigModel;
import io.github.sakurawald.fuji.module.initializer.nametag.structure.NametagEntity;
import io.github.sakurawald.fuji.module.initializer.nametag.structure.NametagEntitySyncer;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

@Document(id = 1751825018627L, value = """
    Customize the nametag above the players.
    """)
@ColorBox(id = 1751978505336L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Set the background of nametag to blue color.
    Set `background` to `-16776961` (The integer representation of blue color)

    ◉ Set the half transparency for nametag.
    Set `text_opacity` to `128`.

    ◉ Scale the size of text into double.
    Set the `x`, `y`, and `z` in `scale` to `2.0`.
    """)
@TestCase(action = "Pass through a nether portal.", targets = {
    "The nametag entity should be discarded in the old dimension."
    , "A new nametag entity should be created in the new dimension."
})
public class NametagInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<NametagConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, NametagConfigModel.class);

    public static Map<ServerPlayerEntity, NametagEntity> nametagEntityMap;

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

    @Override
    protected void onInitialize() {
        nametagEntityMap = new ConcurrentHashMap<>();
    }

    @Override
    protected void onReload() {
        LogUtil.debug("Remove all the created nametag entities. (Reason: module reloaded)");
        nametagEntityMap.values().forEach(NametagEntity::setRemoved);
    }

    @EventConsumer(injectorPriority = EventConsumer.HIGHEST)
    private static void consumePlayerTeleportPreEvent(PlayerTeleportPreEvent event) {
        if (event.getCallbackInfo().isCancelled()) return;
        Optional
            .ofNullable(nametagEntityMap.get(event.getPlayer()))
            .ifPresent(NametagEntity::setRemoved);
    }

}
