package io.github.sakurawald.fuji.module.initializer.pvp;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.pvp.config.model.PvPDataModel;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Set;

@Document(id = 1751826840711L, value = """
    Provides PvP management for players.
    """)
public class PvpInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<PvPDataModel> data = new ObjectConfigurationHandler<>("pvp.json", PvPDataModel.class);

    @Document(id = 1751826842506L, value = "Enable the PvP for the player.")
    @CommandNode("pvp on")
    private static int $on(@CommandSource @CommandTarget ServerPlayerEntity player) {
        Set<String> whitelist = data.model().whitelist;
        String playerName = PlayerHelper.getPlayerName(player);

        if (!whitelist.contains(playerName)) {
            whitelist.add(playerName);
            data.writeStorage();

            TextHelper.sendTextByKey(player, "pvp.on");
            return CommandHelper.Return.SUCCESS;
        }

        TextHelper.sendTextByKey(player, "pvp.on.already");
        return CommandHelper.Return.FAILURE;
    }

    @Document(id = 1751826844847L, value = "Disable the PvP for the player.")
    @CommandNode("pvp off")
    private static int $off(@CommandSource @CommandTarget ServerPlayerEntity player) {
        Set<String> whitelist = data.model().whitelist;
        String playerName = PlayerHelper.getPlayerName(player);

        if (whitelist.contains(playerName)) {
            whitelist.remove(playerName);
            data.writeStorage();

            TextHelper.sendTextByKey(player, "pvp.off");
            return CommandHelper.Return.SUCCESS;
        }

        TextHelper.sendTextByKey(player, "pvp.off.already");
        return CommandHelper.Return.FAILURE;
    }

    @Document(id = 1751826847105L, value = "Query the status of PvP for the player.")
    @CommandNode("pvp status")
    private static int $status(@CommandSource @CommandTarget ServerPlayerEntity player) {
        Set<String> whitelist = data.model().whitelist;
        String playerName = PlayerHelper.getPlayerName(player);

        boolean flag = whitelist.contains(playerName);
        TextHelper.sendText(player, TextHelper.getTextByKey(player, flag ? "pvp.status.on" : "pvp.status.off"));
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826854234L, value = "List the players that enable the PvP.")
    @CommandNode("pvp list")
    private static int $list(@CommandSource ServerCommandSource source) {
        Set<String> whitelist = data.model().whitelist;
        TextHelper.sendTextByKey(source, "pvp.list", whitelist);
        return CommandHelper.Return.SUCCESS;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isPvpEnabled(String name) {
        return data.model().whitelist.contains(name);
    }

}
