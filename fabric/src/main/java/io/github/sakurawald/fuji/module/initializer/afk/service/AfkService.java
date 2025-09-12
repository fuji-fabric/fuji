package io.github.sakurawald.fuji.module.initializer.afk.service;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PacketHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.player.ModifyPlayerListNameEvent;
import io.github.sakurawald.fuji.core.event.message.player.PlayerJoinedEvent;
import io.github.sakurawald.fuji.core.extension.PlayerCombatExtension;
import io.github.sakurawald.fuji.module.initializer.afk.AfkInitializer;
import io.github.sakurawald.fuji.module.initializer.afk.config.model.AfkConfigModel;
import io.github.sakurawald.fuji.module.initializer.afk.structure.PlayerAfkState;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.MovementType;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class AfkService {

    private static final ConcurrentHashMap<String, PlayerAfkState> playerPreviousInputCounterMap = new ConcurrentHashMap<>();

    @TestCase(action = "Issue `/afk` and see the player list.", targets = "The display name of an afk player should be modified.")
    @EventConsumer(injectorPriority = EventConsumer.HIGHEST, consumerPriority = EventConsumer.HIGHEST)
    private static void modifyPlayerListName(ModifyPlayerListNameEvent event) {
        ServerPlayerEntity player = event.getPlayer();
        if (isAfk(player)) {
            Text newValue = getAfkText(player);
            event.setText(newValue);
        }
    }

    public static boolean isAfk(@NotNull ServerPlayerEntity player) {
        return getPlayerAfkState(player)
            .isAfk();
    }

    public static void countAction(@NotNull ServerPlayerEntity player) {
        PlayerAfkState playerAfkState = getPlayerAfkState(player);

        /* Set afk flag to false, once receive any input action. */
        if (playerAfkState.isAfk()) {
            changeAfk(player, false);
        }
    }

    public static @NotNull Text getAfkText(@NotNull ServerPlayerEntity player) {
        return TextHelper.getTextByValue(player, AfkInitializer.config.model().afk_display_name_format);
    }

    public static boolean isPlayerMovedBySelf(@NotNull MovementType movementType, @NotNull Vec3d vec3d) {
        if (movementType == MovementType.PLAYER) {
            // NOTE: In Minecraft's protocol, the client will send the velocity update packet even for (0, 0, 0)
            return Double.compare(vec3d.x, 0) != 0
                || Double.compare(vec3d.y, 0) != 0
                || Double.compare(vec3d.z, 0) != 0;
        }

        return false;
    }

    private static @NotNull PlayerAfkState getPlayerAfkState(@NotNull ServerPlayerEntity player) {
        String playerName = PlayerHelper.getPlayerName(player);
        return playerPreviousInputCounterMap.computeIfAbsent(playerName, k -> new PlayerAfkState());
    }

    private static void resetPlayerAfkState(@NotNull ServerPlayerEntity player) {
        String playerName = PlayerHelper.getPlayerName(player);
        playerPreviousInputCounterMap.put(playerName, new PlayerAfkState());
    }

    public static long getPreviousInputCounter(@NotNull ServerPlayerEntity player) {
        return getPlayerAfkState(player).getPreviousInputCounter();
    }

    public static void setPreviousInputCounter(@NotNull ServerPlayerEntity player, long value) {
        getPlayerAfkState(player).setPreviousInputCounter(value);
    }

    public static void changeAfk(@NotNull ServerPlayerEntity player, boolean flag) {
        // Check if the player can enter afk.
        if (flag && !canEnterAfk(player)) {
            return;
        }

        // Change afk flag.
        PlayerAfkState playerAfkState = getPlayerAfkState(player);
        playerAfkState.setAfk(flag);

        // Update tab list name.
        PacketHelper.sendPacketToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player));

        // Trigger afk events.
        AfkConfigModel.AfkEvent afkEvent = AfkInitializer.config.model().afk_event;
        List<String> commandList = playerAfkState.isAfk() ? afkEvent.on_enter_afk : afkEvent.on_leave_afk;
        CommandExecutor.executeBatch(ExtendedCommandSource.asConsole(player.getCommandSource()), commandList);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean canEnterAfk(@NotNull ServerPlayerEntity player) {
        return player.isOnGround()
            && PlayerHelper.isRealPlayer(player)
            && !player.isOnFire()
            && !player.inPowderSnow
            && !((PlayerCombatExtension) player).fuji$inCombat();
    }

    @EventConsumer
    private static void consumePlayerJoinedEvent(PlayerJoinedEvent event) {
        resetPlayerAfkState(event.getPlayer());
    }
}
