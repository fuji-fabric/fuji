package io.github.sakurawald.fuji.module.initializer.temp_ban;

import com.google.common.net.InetAddresses;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.Duration;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GameProfileCollection;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.service.date_parser.DateParser;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
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

@Document(id = 1751980813637L, value = """
    This module provides the `/temp-ban` command.
    So that you can specify `the duration` for `/ban` command.
    """)
@ColorBox(id = 1751980845082L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ All in one example
    Issue: `/temp-ban player Alice 1s2m3h4d5w6M7y bad boy`
    """)



@CommandNode("temp-ban")
@CommandRequirement(level = 4)
public class TempBanInitializer extends ModuleInitializer {

    // NOTE: The recreation of BanCommand and BanIpCommand, but with a specified expiry date.

    @CommandNode("ip")
    private static int $ip(@CommandSource ServerCommandSource source, String ip, Duration expiry, GreedyString reason) throws CommandSyntaxException {

        if (!InetAddresses.isInetAddress(ip)) {
            throw new SimpleCommandExceptionType(Text.translatable("commands.banip.invalid")).create();
        }

        // Add.
        Date expire = DateParser.parseIntoExpirationDate(expiry.getValue());
        BannedIpEntry bannedIpEntry = new BannedIpEntry(ip, null, source.getName(), expire, reason.getValue());
        source.getServer().getPlayerManager().getIpBanList().add(bannedIpEntry);
        source.sendFeedback(() -> Text.translatable("commands.banip.success", ip, bannedIpEntry.getReason()), true);

        // Kick.
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
    private static int $player(@CommandSource ServerCommandSource source, GameProfileCollection collection, Duration expiry, GreedyString reason) {
        MinecraftServer server = source.getServer();
        PlayerManager playerManager = server.getPlayerManager();
        Date expire = DateParser.parseIntoExpirationDate(expiry.getValue());

        for (GameProfile gameProfile : collection.getValue()) {
            // Add.
            BannedPlayerEntry bannedPlayerEntry = new BannedPlayerEntry(gameProfile, null, source.getName(), expire, reason.getValue());
            playerManager.getUserBanList().add(bannedPlayerEntry);
            source.sendFeedback(() -> Text.translatable("commands.ban.success", Text.literal(gameProfile.getName()), bannedPlayerEntry.getReason()), true);

            // Kick.
            ServerPlayerEntity serverPlayerEntity = playerManager.getPlayer(gameProfile.getId());
            if (serverPlayerEntity != null) {
                serverPlayerEntity.networkHandler.disconnect(Text.translatable("multiplayer.disconnect.banned"));
            }
        }

        return CommandHelper.Return.SUCCESS;
    }

}
