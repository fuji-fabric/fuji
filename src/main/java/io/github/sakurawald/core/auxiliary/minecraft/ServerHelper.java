package io.github.sakurawald.core.auxiliary.minecraft;

import com.mojang.brigadier.CommandDispatcher;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ServerHelper {

    @Getter
    @Setter
    private static MinecraftServer server;

    public static Collection<ServerWorld> getWorlds() {
        return getServer().worlds.values();
    }

    public static @Nullable CommandDispatcher<ServerCommandSource> getCommandDispatcher() {
        if (getServer() == null || getServer().getCommandManager() == null) {
            return null;
        }

        return getServer().getCommandManager().getDispatcher();
    }

    public static PlayerManager getPlayerManager() {
        return getServer().getPlayerManager();
    }

    public static List<ServerPlayerEntity> getPlayers() {
        return getPlayerManager().getPlayerList();
    }

    public static @Nullable ServerPlayerEntity getPlayerByName(String name) {
        return getPlayers()
            .stream()
            .filter(it -> it.getGameProfile().getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    public static @Nullable ServerPlayerEntity getPlayerByNameIgnoreCase(String name) {
        return getPlayers()
            .stream()
            .filter(it -> it.getGameProfile().getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }

    public static Optional<ServerPlayerEntity> getPlayerByUuid(UUID uuid) {
        return getPlayers()
            .stream()
            .filter(player -> player.getUuid().equals(uuid))
            .findFirst();
    }

    public static boolean isPlayerOnline(String name) {
        return getPlayerByName(name) != null;
    }

    public static void sendPacketToAll(Packet<?> packet) {
        getPlayerManager().sendToAll(packet);
    }

    @SuppressWarnings("unused")
    public static void sendPacket(Packet<?> packet, ServerPlayerEntity player) {
        player.networkHandler.sendPacket(packet);
    }

    @SuppressWarnings("unused")
    public static void sendPacketToAllExcept(Packet<?> packet, ServerPlayerEntity player) {
        getPlayerManager().getPlayerList().stream().filter(it -> it != player).forEach(p -> p.networkHandler.sendPacket(packet));
    }

    public static void updateDisplayName() {
        MinecraftServer server = getServer();
        for (ServerPlayerEntity player : getPlayers()) {
            server.getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player));
        }
    }
}
