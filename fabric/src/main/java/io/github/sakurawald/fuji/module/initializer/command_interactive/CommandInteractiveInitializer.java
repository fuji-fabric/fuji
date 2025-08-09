package io.github.sakurawald.fuji.module.initializer.command_interactive;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;

#if MC_VER <= MC_1_20_4
import java.time.Instant;
import java.util.BitSet;
import net.minecraft.network.message.ArgumentSignatureDataMap;
import net.minecraft.network.message.LastSeenMessageList;
#endif

import java.util.HashSet;
import java.util.Set;

@Document(id = 1751824965598L, value = """
    This module allows you to write commands in `sign` block.
    And then click to execute commands.
    """)
@ColorBox(id = 1751870448041L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    ◉ How it works?

    If a player `right click` a `sign block`.
    We will check if the `facing texts` contains the `/` character.
    If contains, we will treat as the player issue the command.
    """)
@ColorBox(id = 1751968326784L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    A `sign block` that contains the `character /` is called an `interactive sign block`.
    You can use `right click` to execute the commands written on the interactive sign block.
    You can use `shift + right click` to edit an `interactive sign block`.
    """)
@ColorBox(id = 1751968409125L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    You can write some comment text before the first `character /`.
    All 4 lines will be joined and treated as one single big line.
    So be careful with the `space character`, and ignore the `linefeed character`.
    """)
@TestCase(action = "Test the `command_interactive` module in `online-mode` server.", targets = "The packet should not break the client-side signature validation.")
@TestCase(action = "Enable `command_warmup` module, issue `/back` command.", targets = "It should work with un-signed argument type.")
@TestCase(action = "Enable `command_warmup` module, issue `/say hi` command.", targets = "It should work with signed argument type.")
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
    private static CommandExecutionC2SPacket makeCommandExecutionPacket(String commandString) {
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
        CommandExecutionC2SPacket packet = makeCommandExecutionPacket(commandString);

        /* Trust the packet. */
        addTrustedPacket(packet);

        // NOTE: Call the method directly to simulate a c2s packet.
        player.networkHandler.onCommandExecution(packet);
    }
}
