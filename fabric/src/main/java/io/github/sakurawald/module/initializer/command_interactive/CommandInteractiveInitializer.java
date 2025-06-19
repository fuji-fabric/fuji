package io.github.sakurawald.module.initializer.command_interactive;

import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.network.message.ArgumentSignatureDataMap;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;

import java.time.Instant;
import java.util.BitSet;

public class CommandInteractiveInitializer extends ModuleInitializer {

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
        // NOTE: Call the method directly to simulate a c2s packet.
        player.networkHandler.onCommandExecution(makeCommandExecutionPacket(player, commandString));
    }
}
