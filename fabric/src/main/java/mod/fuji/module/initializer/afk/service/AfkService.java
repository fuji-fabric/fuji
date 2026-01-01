package mod.fuji.module.initializer.afk.service;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.MoverType;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class AfkService {

    private static final ConcurrentHashMap<String, PlayerAfkState> playerAfkStateMap = new ConcurrentHashMap<>();

    @EventConsumer
    private static void consumePlayerJoinedEvent(PlayerJoinedEvent event) {
        resetPlayerAfkState(event.getPlayer());
    }

    @TestCase(action = "Issue `/afk` and see the player list.", targets = "The display name of an afk player should be modified.")
    @EventConsumer(injectorPriority = EventConsumer.HIGHEST, consumerPriority = EventConsumer.HIGHEST)
    private static void modifyPlayerListName(ModifyPlayerListNameEvent event) {
        ServerPlayer player = event.getPlayer();
        if (isInAfkState(player)) {
            Component newValue = getInAfkStateDisplayNameText(player);
            event.setText(newValue);
        }
    }

    public static boolean isInAfkState(@NotNull ServerPlayer player) {
        return getPlayerAfkState(player)
            .isInState();
    }

    public static void receiveAction(@NotNull ServerPlayer player) {
        PlayerAfkState playerAfkState = getPlayerAfkState(player);

        /* Try change the afk state. */
        if (playerAfkState.isInState()) {
            changeAfkState(player, false);
        }
    }

    private static @NotNull Component getInAfkStateDisplayNameText(@NotNull ServerPlayer player) {
        return TextHelper.getTextByValue(player, AfkInitializer.config.model().getAfkDisplayNameFormat());
    }

    public static boolean isPlayerMovedBySelf(@NotNull MoverType movementType, @NotNull Vec3 vec3d) {
        if (movementType == MoverType.PLAYER) {
            // NOTE: In Minecraft's protocol, the client will send the velocity update packet even for (0, 0, 0) motion.
            return Double.compare(vec3d.x, 0) != 0
                || Double.compare(vec3d.y, 0) != 0
                || Double.compare(vec3d.z, 0) != 0;
        }

        return false;
    }

    private static @NotNull PlayerAfkState getPlayerAfkState(@NotNull ServerPlayer player) {
        String playerName = PlayerHelper.getPlayerName(player);
        return playerAfkStateMap.computeIfAbsent(playerName, k -> new PlayerAfkState());
    }

    private static void resetPlayerAfkState(@NotNull ServerPlayer player) {
        String playerName = PlayerHelper.getPlayerName(player);
        playerAfkStateMap.put(playerName, new PlayerAfkState());
    }

    public static long getPreviousInputCounter(@NotNull ServerPlayer player) {
        return getPlayerAfkState(player)
            .getPreviousInputCounter();
    }

    public static void setPreviousInputCounter(@NotNull ServerPlayer player, long value) {
        getPlayerAfkState(player)
            .setPreviousInputCounter(value);
    }

    public static void changeAfkState(@NotNull ServerPlayer player, boolean flag) {
        /* Check if the player can enter afk state. */
        if (flag && !canEnterAfkState(player)) {
            return;
        }

        /* Change the afk state. */
        PlayerAfkState playerAfkState = getPlayerAfkState(player);
        playerAfkState.setInState(flag);

        /* Update the display name. */
        PacketHelper.sendPacketToAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player));

        /* Trigger afk events. */
        AfkConfigModel.AfkEvent afkEvent = AfkInitializer.config.model().getAfkEvent();
        List<String> commandList = playerAfkState.isInState() ? afkEvent.getOnEnterAfk() : afkEvent.getOnLeaveAfk();
        CommandSourceStack commandSource = CommandHelper.Source.getCommandSource(player);
        CommandExecutor.executeBatch(ExtendedCommandSource.asConsole(commandSource), commandList);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean canEnterAfkState(@NotNull ServerPlayer player) {
        return PlayerHelper.Kind.isRealPlayer(player)
            && !player.isOnFire()
            && !player.isInPowderSnow
            && !((PlayerCombatExtension) player).fuji$inCombat();
    }

}
