package io.github.sakurawald.module.initializer.temp_ban;

import com.google.common.net.InetAddresses;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.argument.wrapper.impl.GameProfileCollection;
import io.github.sakurawald.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.core.structure.DateParser;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Date;
import java.util.List;

@CommandNode("temp-ban")
@CommandRequirement(level = 4)
public class TempBanInitializer extends ModuleInitializer {

    @CommandNode("ip")
    private static int ip(@CommandSource ServerCommandSource source, String ip, String expiry, GreedyString reason) throws CommandSyntaxException {

        if (!InetAddresses.isInetAddress(ip)) {
            throw new SimpleCommandExceptionType(Text.translatable("commands.banip.invalid")).create();
        }

        // add
        Date expire = DateParser.parseDate(expiry);
        BannedIpEntry bannedIpEntry = new BannedIpEntry(ip, null, source.getName(), expire, reason.getValue());
        source.getServer().getPlayerManager().getIpBanList().add(bannedIpEntry);
        source.sendFeedback(() -> Text.translatable("commands.banip.success", ip, bannedIpEntry.getReason()), true);

        // feedback
        List<ServerPlayerEntity> list = source.getServer().getPlayerManager().getPlayersByIp(ip);
        if (!list.isEmpty()) {
            source.sendFeedback(() -> Text.translatable("commands.banip.info", list.size(), EntitySelector.getNames(list)), true);
        }
        for (ServerPlayerEntity serverPlayerEntity : list) {
            serverPlayerEntity.networkHandler.disconnect(Text.translatable("multiplayer.disconnect.ip_banned"));
        }

        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("player")
    private static int player(@CommandSource ServerCommandSource source, GameProfileCollection collection, String expiry, GreedyString reason) {
        MinecraftServer server = source.getServer();
        PlayerManager playerManager = server.getPlayerManager();
        Date expire = DateParser.parseDate(expiry);

        for (GameProfile gameProfile : collection.getValue()) {
            // add
            BannedPlayerEntry bannedPlayerEntry = new BannedPlayerEntry(gameProfile, null, source.getName(), expire, reason.getValue());
            playerManager.getUserBanList().add(bannedPlayerEntry);
            source.sendFeedback(() -> Text.translatable("commands.ban.success", Text.literal(gameProfile.getName()), bannedPlayerEntry.getReason()), true);

            // kick
            ServerPlayerEntity serverPlayerEntity = playerManager.getPlayer(gameProfile.getId());
            if (serverPlayerEntity != null) {
                serverPlayerEntity.networkHandler.disconnect(Text.translatable("multiplayer.disconnect.banned"));
            }
        }

        return CommandHelper.Return.SUCCESS;
    }

}
