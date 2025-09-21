package mod.fuji.core.auxiliary.minecraft;

import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class PacketHelper {

    public static void sendPacket(@NotNull Packet<?> packet, @NotNull ServerPlayerEntity player) {
        player.networkHandler.sendPacket(packet);
    }

    public static void sendPacketToAll(@NotNull Packet<?> packet) {
        PlayerHelper.getPlayerManager()
            .sendToAll(packet);
    }

    @SuppressWarnings("unused")
    public static void sendPacketToAllExcept(@NotNull Packet<?> packet, @NotNull ServerPlayerEntity player) {
        PlayerHelper.getPlayerManager()
            .getPlayerList()
            .stream()
            .filter(it -> !it.equals(player))
            .forEach(p -> sendPacket(packet, player));
    }
}
