package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;

public class PacketHelper {

    public static void sendPacketToAll(Packet<?> packet) {
        PlayerHelper.getPlayerManager()
            .sendToAll(packet);
    }

    public static void sendPacket(Packet<?> packet, ServerPlayerEntity player) {
        player.networkHandler.sendPacket(packet);
    }

    @SuppressWarnings("unused")
    public static void sendPacketToAllExcept(Packet<?> packet, ServerPlayerEntity player) {
        PlayerHelper.getPlayerManager()
            .getPlayerList()
            .stream()
            .filter(it -> !it.equals(player))
            .forEach(p -> sendPacket(packet, player));
    }
}
