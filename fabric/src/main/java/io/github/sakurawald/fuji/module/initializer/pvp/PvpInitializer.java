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
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.player.PlayerDamageEvent;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.pvp.config.model.PvPDataModel;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Set;

@Document(id = 1751826840711L, value = """
    Provides PvP management for players.
    """)
public class PvpInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<PvPDataModel> data = ObjectConfigurationHandler.ofModule("pvp.json", PvPDataModel.class);

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
        TextHelper.sendMessageByText(player, TextHelper.getTextByKey(player, flag ? "pvp.status.on" : "pvp.status.off"));
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

    @EventConsumer
    private static void processPvpDamage(PlayerDamageEvent event) {
        Entity damageSourceEntity = event.getDamageSource().getSource();
        if (damageSourceEntity instanceof ServerPlayerEntity damageSourcePlayer) {
            /* Don't flint a TNT to kill yourself. */
            if (damageSourceEntity.equals(event.getPlayer())) return;

            /* Okay, the damage source player should enable pvp first. */
            String damageSourcePlayerName = PlayerHelper.getPlayerName(damageSourcePlayer);
            if (!PvpInitializer.isPvpEnabled(damageSourcePlayerName)) {
                TextHelper.sendTextByKey(damageSourcePlayer, "pvp.check.off.me");
                event.setDamage(0);
                return;
            }

            /* Then, the damage target player should enable pvp. */
            String damageTargetPlayerName = PlayerHelper.getPlayerName(event.getPlayer());
            if (!PvpInitializer.isPvpEnabled(damageTargetPlayerName)) {
                TextHelper.sendTextByKey(damageSourcePlayer, "pvp.check.off.others", damageTargetPlayerName);
                event.setDamage(0);
            }
        }

    }

}
