package io.github.sakurawald.module.initializer.pvp;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.annotation.CommandTarget;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.pvp.config.model.PvPDataModel;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Set;

@Document("""
    Provides PvP management for players.
    """)
public class PvpInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<PvPDataModel> data = new ObjectConfigurationHandler<>("pvp.json", PvPDataModel.class);

    @CommandNode("pvp on")
    private static int $on(@CommandSource @CommandTarget ServerPlayerEntity player) {
        Set<String> whitelist = data.model().whitelist;
        String playerName = PlayerHelper.getName(player);

        if (!whitelist.contains(playerName)) {
            whitelist.add(playerName);
            data.writeStorage();

            TextHelper.sendMessageByKey(player, "pvp.on");
            return CommandHelper.Return.SUCCESS;
        }

        TextHelper.sendMessageByKey(player, "pvp.on.already");
        return CommandHelper.Return.FAIL;
    }

    @CommandNode("pvp off")
    private static int $off(@CommandSource @CommandTarget ServerPlayerEntity player) {
        Set<String> whitelist = data.model().whitelist;
        String playerName = PlayerHelper.getName(player);

        if (whitelist.contains(playerName)) {
            whitelist.remove(playerName);
            data.writeStorage();

            TextHelper.sendMessageByKey(player, "pvp.off");
            return CommandHelper.Return.SUCCESS;
        }

        TextHelper.sendMessageByKey(player, "pvp.off.already");
        return CommandHelper.Return.FAIL;
    }

    @CommandNode("pvp status")
    private static int $status(@CommandSource @CommandTarget ServerPlayerEntity player) {
        Set<String> whitelist = data.model().whitelist;
        String playerName = PlayerHelper.getName(player);

        boolean flag = whitelist.contains(playerName);
        player.sendMessage(TextHelper.getTextByKey(player, flag ? "pvp.status.on" : "pvp.status.off"));
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("pvp list")
    private static int $list(@CommandSource ServerCommandSource source) {
        Set<String> whitelist = data.model().whitelist;
        TextHelper.sendMessageByKey(source, "pvp.list", whitelist);
        return CommandHelper.Return.SUCCESS;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isPvpEnabled(String name) {
        return data.model().whitelist.contains(name);
    }

}
