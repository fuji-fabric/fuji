package mod.fuji.module.initializer.afk.service;

import mod.fuji.core.auxiliary.minecraft.PacketHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.executor.CommandExecutor;
import mod.fuji.core.command.executor.structure.ExtendedCommandSource;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.ModifyPlayerListNameEvent;
import mod.fuji.core.event.message.player.PlayerJoinedEvent;
import mod.fuji.core.extension.PlayerCombatExtension;
import mod.fuji.module.initializer.afk.AfkInitializer;
import mod.fuji.module.initializer.afk.config.model.AfkConfigModel;
import mod.fuji.module.initializer.afk.structure.PlayerAfkState;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.MoverType;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class AfkService {

    private static final ConcurrentHashMap<String, PlayerAfkState> playerPreviousInputCounterMap = new ConcurrentHashMap<>();

    @TestCase(action = "Issue `/afk` and see the player list.", targets = "The display name of an afk player should be modified.")
    @EventConsumer(injectorPriority = EventConsumer.HIGHEST, consumerPriority = EventConsumer.HIGHEST)
    private static void modifyPlayerListName(ModifyPlayerListNameEvent event) {
        ServerPlayer player = event.getPlayer();
        if (isAfk(player)) {
            Component newValue = getAfkText(player);
            event.setText(newValue);
        }
    }

    public static boolean isAfk(@NotNull ServerPlayer player) {
        return getPlayerAfkState(player)
            .isAfk();
    }

    public static void countAction(@NotNull ServerPlayer player) {
        PlayerAfkState playerAfkState = getPlayerAfkState(player);

        /* Set afk flag to false, once receive any input action. */
        if (playerAfkState.isAfk()) {
            changeAfk(player, false);
        }
    }

    public static @NotNull Component getAfkText(@NotNull ServerPlayer player) {
        return TextHelper.getTextByValue(player, AfkInitializer.config.model().afk_display_name_format);
    }

    public static boolean isPlayerMovedBySelf(@NotNull MoverType movementType, @NotNull Vec3 vec3d) {
        if (movementType == MoverType.PLAYER) {
            // NOTE: In Minecraft's protocol, the client will send the velocity update packet even for (0, 0, 0)
            return Double.compare(vec3d.x, 0) != 0
                || Double.compare(vec3d.y, 0) != 0
                || Double.compare(vec3d.z, 0) != 0;
        }

        return false;
    }

    private static @NotNull PlayerAfkState getPlayerAfkState(@NotNull ServerPlayer player) {
        String playerName = PlayerHelper.getPlayerName(player);
        return playerPreviousInputCounterMap.computeIfAbsent(playerName, k -> new PlayerAfkState());
    }

    private static void resetPlayerAfkState(@NotNull ServerPlayer player) {
        String playerName = PlayerHelper.getPlayerName(player);
        playerPreviousInputCounterMap.put(playerName, new PlayerAfkState());
    }

    public static long getPreviousInputCounter(@NotNull ServerPlayer player) {
        return getPlayerAfkState(player).getPreviousInputCounter();
    }

    public static void setPreviousInputCounter(@NotNull ServerPlayer player, long value) {
        getPlayerAfkState(player).setPreviousInputCounter(value);
    }

    public static void changeAfk(@NotNull ServerPlayer player, boolean flag) {
        // Check if the player can enter afk.
        if (flag && !canEnterAfk(player)) {
            return;
        }

        // Change afk flag.
        PlayerAfkState playerAfkState = getPlayerAfkState(player);
        playerAfkState.setAfk(flag);

        // Update tab list name.
        PacketHelper.sendPacketToAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player));

        // Trigger afk events.
        AfkConfigModel.AfkEvent afkEvent = AfkInitializer.config.model().afk_event;
        List<String> commandList = playerAfkState.isAfk() ? afkEvent.on_enter_afk : afkEvent.on_leave_afk;
        CommandExecutor.executeBatch(ExtendedCommandSource.asConsole(player.createCommandSourceStack()), commandList);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean canEnterAfk(@NotNull ServerPlayer player) {
        return PlayerHelper.Kind.isRealPlayer(player)
            && !player.isOnFire()
            && !player.isInPowderSnow
            && !((PlayerCombatExtension) player).fuji$inCombat();
    }

    @EventConsumer
    private static void consumePlayerJoinedEvent(PlayerJoinedEvent event) {
        resetPlayerAfkState(event.getPlayer());
    }
}
