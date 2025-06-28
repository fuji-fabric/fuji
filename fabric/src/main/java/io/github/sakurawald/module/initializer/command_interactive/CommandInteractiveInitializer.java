package io.github.sakurawald.module.initializer.command_interactive;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.structure.descriptor.annotation.ColorBox;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.network.message.ArgumentSignatureDataMap;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;

import java.time.Instant;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

@Document("""
    This module allows you to write commands in `sign` block.
    And then click to execute commands.
    """)

@ColorBox(color = ColorBox.ColorBlockTypes.TIPS, value = """
    How it works?

    If a player `right click` a `sign block`.
    We will check if the `facing texts` contains the `/` character.
    If contains, we will treat as the player issue the command.
    """)


public class CommandInteractiveInitializer extends ModuleInitializer {

    // NOTE: It's annoy, see https://gist.github.com/kennytv/ed783dd244ca0321bbd882c347892874
    private static final Set<Packet<?>> TRUSTED_PACKETS = new HashSet<>();

    public static void addTrustedPacket(Packet<?> packet) {
        TRUSTED_PACKETS.add(packet);
    }

    public static void removeTrustedPacket(Packet<?> packet) {
        TRUSTED_PACKETS.remove(packet);
    }

    public static boolean isTrustedPacket(Packet<?> packet) {
        return TRUSTED_PACKETS.contains(packet);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private static CommandExecutionC2SPacket makeCommandExecutionPacket(ServerPlayerEntity player, String commandString) {
        #if MC_VER <= MC_1_20_4
        var ack = new LastSeenMessageList.Acknowledgment(0, new BitSet());
        CommandExecutionC2SPacket packet = new CommandExecutionC2SPacket(commandString, Instant.now(), 0L, ArgumentSignatureDataMap.EMPTY, ack);
        #elif MC_VER > MC_1_20_4
        CommandExecutionC2SPacket packet = new CommandExecutionC2SPacket(commandString);
        #endif
        return packet;
    }

    public static void mimicCommandExecutionPacket(ServerPlayerEntity player, String commandString) {
        /* Make command execution packet. */
        CommandExecutionC2SPacket packet = makeCommandExecutionPacket(player, commandString);

        /* Trust the packet. */
        addTrustedPacket(packet);

        // NOTE: Call the method directly to simulate a c2s packet.
        player.networkHandler.onCommandExecution(packet);
    }
}
