package mod.fuji.module.initializer.pvp;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.PlayerDamageEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.pvp.config.model.PvPDataModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

@Document(id = 1751826840711L, value = """
    Provides PvP management for players.
    """)
public class PvpInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<PvPDataModel> data = ObjectConfigurationHandler.ofModule("pvp.json", PvPDataModel.class);

    @Document(id = 1751826842506L, value = "Enable the PvP for the player.")
    @CommandNode("pvp on")
    private static int $on(@CommandSource @CommandTarget ServerPlayer player) {
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
    private static int $off(@CommandSource @CommandTarget ServerPlayer player) {
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
    private static int $status(@CommandSource @CommandTarget ServerPlayer player) {
        Set<String> whitelist = data.model().whitelist;
        String playerName = PlayerHelper.getPlayerName(player);

        boolean flag = whitelist.contains(playerName);
        TextHelper.sendMessageByText(player, TextHelper.getTextByKey(player, flag ? "pvp.status.on" : "pvp.status.off"));
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826854234L, value = "List the players that enable the PvP.")
    @CommandNode("pvp list")
    private static int $list(@CommandSource CommandSourceStack source) {
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
        Entity damageSourceEntity = event.getDamageSource().getDirectEntity();
        if (damageSourceEntity instanceof ServerPlayer damageSourcePlayer) {
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
