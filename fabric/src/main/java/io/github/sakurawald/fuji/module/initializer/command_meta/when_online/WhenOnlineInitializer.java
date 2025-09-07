package io.github.sakurawald.fuji.module.initializer.command_meta.when_online;

import io.github.sakurawald.fuji.core.annotation.Unused;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.OfflinePlayerName;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.player.PlayerJoinedEvent;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_meta.when_online.config.model.WhenOnlineDataModel;
import io.github.sakurawald.fuji.module.initializer.command_meta.when_online.gui.ListWhenOnlineTicketsGui;
import io.github.sakurawald.fuji.module.initializer.command_meta.when_online.structure.WhenOnlineTicket;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

@Document(id = 1751824024333L, value = """
    This module provides the `/when-online \\<player\\> \\<cmd\\>` command.
    To execute a specified command `exactly once` when the target player is `online`:
    1. If the target player is `online` now, the command will be executed `at once`.
    2. If the target player is `offline` now, the command will be executed `when the player online`.

    Besides, you use `/when-online list` to manage `submitted commands`.

    For example:
    1. `/when-online Steve give %player:name% minecraft:apple 3`
    2. `/when-online Alex delay 8 say Hi %player:name%`
    """)
public class WhenOnlineInitializer extends ModuleInitializer {

    public static BaseConfigurationHandler<WhenOnlineDataModel> data = ObjectConfigurationHandler.ofModule("when-online-data.json", WhenOnlineDataModel.class);

    @Document(id = 1755412463665L, value = "Execute the specified command `exactly once`, when the target player `is online`.")
    @CommandNode("when-online")
    @CommandRequirement(level = 4)
    private static int $whenOnline(@CommandSource ServerCommandSource source, OfflinePlayerName targetPlayer, GreedyString command) {
        String $creatorName = source.getName();
        String $targetPlayerName = targetPlayer.getValue();
        String $command = command.getValue();

        WhenOnlineTicket ticket = WhenOnlineTicket.make($creatorName, $targetPlayerName, $command);
        data.model().tickets.add(ticket);
        data.writeStorage();

        processWhenOnlineTickets();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1755412352252L, value = "An alias command for `/when-online list` command.")
    @CommandNode("when-online")
    @CommandRequirement(level = 4)
    private static int $root(@CommandSource ServerPlayerEntity player) {
        return $list(player);
    }


    @Document(id = 1755412381305L, value = "List all submitted `when-online` tickets.")
    @CommandNode("when-online list")
    @CommandRequirement(level = 4)
    private static int $list(@CommandSource ServerPlayerEntity player) {
        ListWhenOnlineTicketsGui
            .make(player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @EventConsumer
    private static void processWhenOnlineTickets(@Unused PlayerJoinedEvent event) {
        processWhenOnlineTickets();
    }

    private static void processWhenOnlineTickets() {
        /* Get the online players. */
        List<String> onlinePlayerNames = PlayerHelper.Lookup.getOnlinePlayerNames();

        /* Find un-executed tickets, and match it with online players. */
        data.model().tickets
            .stream()
            // Make a new List, to support recursion. (e.g. `/when-online Steve when-online %player:name% say Triggered.`)
            .toList()
            .stream()
            .filter(ticket -> ticket.executedTimestamp == null
                && onlinePlayerNames.contains(ticket.targetPlayer))
            .forEach(ticket -> {
                LogUtil.debug("Execute the ticket: {}", ticket);

                /* Consume the ticket. */
                ticket.executedTimestamp = System.currentTimeMillis();

                /* Execute the specified command. */
                Optional<ServerPlayerEntity> onlinePlayer = PlayerHelper.Lookup.getOnlinePlayerByName(ticket.targetPlayer);
                onlinePlayer.ifPresentOrElse($onlinePlayer -> {
                    ExtendedCommandSource extendedCommandSource = ExtendedCommandSource.asConsole($onlinePlayer.getCommandSource());
                    String commandString = ticket.command;
                    CommandExecutor.executeSingle(extendedCommandSource, commandString);
                }, () -> LogUtil.warn("Failed to execute the when-online ticket, the online player is null.", ticket));
            });

        /* Save the result. */
        data.writeStorage();
    }


}
