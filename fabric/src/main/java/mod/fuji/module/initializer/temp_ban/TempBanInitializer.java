package mod.fuji.module.initializer.temp_ban;

import com.google.common.net.InetAddresses;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Optional;
import mod.fuji.core.auxiliary.minecraft.AuthlibHelper;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.config.mapper.representation.GameProfileIR;
import mod.fuji.core.service.duration_parser.command.argument.wrapper.Duration;
import mod.fuji.core.command.argument.wrapper.impl.GameProfileCollection;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.service.duration_parser.DurationParser;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.Date;
import java.util.List;

@Document(id = 1751980813637L, value = """
    This module provides the `/temp-ban` command.
    So that you can specify `the duration` for `/ban` command.
    """)
@ColorBox(id = 1751980845082L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ All in one example
    Issue: `/temp-ban player Alice 1s2m3h4d5w6M7y bad boy`
    """)



@CommandNode("temp-ban")
@CommandRequirement(level = 4)
public class TempBanInitializer extends ModuleInitializer {

    // NOTE: The recreation of BanCommand and BanIpCommand, but with a specified expiry date.

    @CommandNode("ip")
    private static int $ip(@CommandSource CommandSourceStack source, String ip, Duration expiry, GreedyString reason) throws CommandSyntaxException {

        if (!InetAddresses.isInetAddress(ip)) {
            throw new SimpleCommandExceptionType(Component.translatable("commands.banip.invalid")).create();
        }

        // Add.
        Date expire = DurationParser.parseIntoExpirationDate(expiry.getValue()).orElseThrow();
        IpBanListEntry bannedIpEntry = new IpBanListEntry(ip, null, source.getTextName(), expire, reason.getValue());
        source.getServer().getPlayerList().getIpBans().add(bannedIpEntry);
        source.sendSuccess(() -> Component.translatable("commands.banip.success", ip, bannedIpEntry.getReason()), true);

        // Kick.
        List<ServerPlayer> list = source.getServer().getPlayerList().getPlayersWithAddress(ip);
        if (!list.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.banip.info", list.size(), EntitySelector.joinNames(list)), true);
        }
        for (ServerPlayer serverPlayerEntity : list) {
            serverPlayerEntity.connection.disconnect(Component.translatable("multiplayer.disconnect.ip_banned"));
        }

        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("player")
    private static int $player(@CommandSource CommandSourceStack source, GameProfileCollection collection, Duration expiry, GreedyString reason) {
        MinecraftServer server = source.getServer();
        PlayerList playerManager = server.getPlayerList();
        Date expire = DurationParser.parseIntoExpirationDate(expiry.getValue()).orElseThrow();

        for (GameProfile gameProfile : collection.getValue()
            .stream()
            .map(GameProfileIR::from)
            .map(GameProfileIR::toGameProfile)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList()) {

            // Add.
            GameProfileIR gameProfileIR = GameProfileIR.from(gameProfile);

            UserBanListEntry bannedPlayerEntry = new UserBanListEntry(gameProfileIR.toUserProfile().orElseThrow(), null, source.getTextName(), expire, reason.getValue());
            playerManager.getBans().add(bannedPlayerEntry);
            source.sendSuccess(() -> Component.translatable("commands.ban.success", Component.literal(AuthlibHelper.getGameProfileName(gameProfile)), bannedPlayerEntry.getReason()), true);

            // Kick.
            ServerPlayer serverPlayerEntity = playerManager.getPlayer(AuthlibHelper.getGameProfileId(gameProfile));
            if (serverPlayerEntity != null) {
                serverPlayerEntity.connection.disconnect(Component.translatable("multiplayer.disconnect.banned"));
            }
        }

        return CommandHelper.Return.SUCCESS;
    }

}
