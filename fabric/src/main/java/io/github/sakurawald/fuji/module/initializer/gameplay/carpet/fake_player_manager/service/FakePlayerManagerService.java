package io.github.sakurawald.fuji.module.initializer.gameplay.carpet.fake_player_manager.service;

import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.gameplay.carpet.fake_player_manager.FakePlayerManagerInitializer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FakePlayerManagerService {

    public static final Map<String, List<String>> player2fakePlayers = new HashMap<>();
    public static final Map<String, Long> player2expiration = new HashMap<>();

    private static <T> T withMyFakePlayers(@Nullable ServerPlayerEntity player, Function<List<String>, T> function) {
        String playerName = getOwnerPlayerName(player);
        List<String> fakePlayers = player2fakePlayers.computeIfAbsent(playerName, k -> new ArrayList<>());
        return function.apply(fakePlayers);
    }

    private static String getOwnerPlayerName(@Nullable ServerPlayerEntity player) {
        return player == null ? "CONSOLE" : PlayerHelper.getPlayerName(player);
    }

    public static void addMyFakePlayer(@NotNull ServerPlayerEntity player, @NotNull String fakePlayerName) {
        withMyFakePlayers(player, fakePlayers -> fakePlayers.add(fakePlayerName));
    }

    public static boolean isMyFakePlayer(@NotNull ServerPlayerEntity player, @NotNull String fakePlayerName) {
        return withMyFakePlayers(player, fakePlayers ->
            fakePlayers
                .stream()
                // NOTE: The `carpet` mod ignores the case of fake-player name.
                .anyMatch(it -> it.equalsIgnoreCase(fakePlayerName)));
    }

    public static void renewMyFakePlayers(@Nullable ServerPlayerEntity player) {
        int renewDuration = FakePlayerManagerInitializer.config.model().renew_duration_ms;
        long newExpirationTime = System.currentTimeMillis() + renewDuration;
        String playerName = getOwnerPlayerName(player);
        player2expiration.put(playerName, newExpirationTime);

        if (player != null) {
            TextHelper.sendTextByKey(player, "fake_player_manager.renew.success", ChronosUtil.toDefaultDateFormat(newExpirationTime));
        }
    }

    public static boolean canSpawnNewFakePlayer(@NotNull ServerPlayerEntity player) {
        int capsLimit = getFakePlayerCapsLimit();
        int currentQuantity = withMyFakePlayers(player, List::size);
        return currentQuantity < capsLimit;
    }

    public static int getFakePlayerCapsLimit() {
        int currentDays = LocalDate.now().getDayOfWeek().getValue();
        LocalTime currentTime = LocalTime.now();
        int currentMinutes = currentTime.getHour() * 60 + currentTime.getMinute();

        Optional<List<Integer>> first = FakePlayerManagerInitializer.config.model()
            .caps_limit_rule
            .stream()
            .filter(it -> currentDays >= it.get(0) && currentMinutes >= it.get(1))
            .findFirst();
        return first
            .map(it -> it.get(2))
            .orElse(-1);
    }

    public static boolean canManipulateFakePlayer(@NotNull CommandContext<ServerCommandSource> context, String fakePlayerName) {
        // NOTE: Disables the `/player <player> shadow` command, to prevent it is used on an online player.
        if (context.getNodes().get(2).getNode().getName().equals("shadow")) return false;

        // The console is considered authorized.
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return true;

        // The op is considered authorized.
        if (context.getSource().hasPermissionLevel(4)) return true;

        // The owner player is considered authorized.
        return isMyFakePlayer(player, fakePlayerName);
    }

    public static @NotNull String getTransformedFakePlayerName(@NotNull String fakePlayerName) {
        return FakePlayerManagerInitializer.config.model().transform_name
            .replace("%name%", fakePlayerName)
            .replace("%s", fakePlayerName);
    }
}
