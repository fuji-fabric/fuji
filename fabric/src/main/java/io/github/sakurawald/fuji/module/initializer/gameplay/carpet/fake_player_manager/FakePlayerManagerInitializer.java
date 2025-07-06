package io.github.sakurawald.fuji.module.initializer.gameplay.carpet.fake_player_manager;

import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.gameplay.carpet.fake_player_manager.config.model.FakePlayerManagerConfigModel;
import io.github.sakurawald.fuji.module.initializer.gameplay.carpet.fake_player_manager.job.ManageFakePlayersJob;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Document("""
    This module provides `fake player management` for `carpet` mod.
    """)
public class FakePlayerManagerInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<FakePlayerManagerConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, FakePlayerManagerConfigModel.class);

    private static final Map<String, List<String>> player2fakePlayers = new HashMap<>();
    private static final Map<String, Long> player2expiration = new HashMap<>();

    public static void checkCapsLimit() {
        /* invalid */
        invalidFakePlayers();

        /* update value */
        int capsLimit = computeFakePlayerCapsLimit();
        long currentTimeMs = System.currentTimeMillis();

        player2fakePlayers.entrySet()
            .forEach(e -> {
                String ownerPlayerName = e.getKey();

                /* make new value */
                long expiration = player2expiration.computeIfAbsent(ownerPlayerName, k -> 0L);
                final Integer[] allowFakePlayers = {0};
                List<String> newValue = e.getValue()
                    .stream()
                    .filter(fakePlayerName -> {
                        ServerPlayerEntity fakePlayer = ServerHelper.getOnlinePlayerByNameIgnoreCase(fakePlayerName);
                        if (fakePlayer == null) return false;

                        /* check: expiration */
                        if (currentTimeMs >= expiration) {
                            /* auto-renew the fake players if the owner player is online */
                            ServerPlayerEntity owner = ServerHelper.getOnlinePlayerByNameIgnoreCase(ownerPlayerName);
                            if (owner != null) {
                                renewMyFakePlayers(owner);
                                return true;
                            }

                            /* kill all fake players due to expiration */
                            EntityHelper.killEntity(fakePlayer);
                            TextHelper.sendBroadcastByKey("fake_player_manager.kick_for_expiration", fakePlayer.getGameProfile().getName(), ownerPlayerName);
                            return false;
                        }

                        /* check: caps */
                        if (allowFakePlayers[0] < capsLimit) {
                            allowFakePlayers[0]++;
                            return true;
                        } else {
                            EntityHelper.killEntity(fakePlayer);
                            TextHelper.sendBroadcastByKey("fake_player_manager.kick_for_amount", fakePlayer.getGameProfile().getName(), ownerPlayerName);
                            return false;
                        }

                    }).collect(Collectors.toList());

                /* set new value */
                e.setValue(newValue);
            });

    }

    @Document("Renew the expiration time of all fake-players spawned by you.")
    @CommandNode("player renew")
    private static int $renew(@CommandSource ServerPlayerEntity player) {
        renewMyFakePlayers(player);
        return CommandHelper.Return.SUCCESS;
    }

    @Document("List all fake-players and its owner.")
    @CommandNode("player who")
    private static int $who(@CommandSource CommandContext<ServerCommandSource> context) {
        /* make table */
        StringBuilder body = new StringBuilder();
        player2fakePlayers.forEach((k, v) -> body.append("%s -> %s".formatted(k, v))
            .append("\n"));

        /* make text */
        ServerCommandSource source = context.getSource();
        source.sendMessage(
            TextHelper.getTextByKey(source, "fake_player_manager.who.header")
                .copy()
                .append(Text.literal(body.toString())));
        return CommandHelper.Return.SUCCESS;
    }

    public static void renewMyFakePlayers(@NotNull ServerPlayerEntity player) {
        int renewDuration = config.model().renew_duration_ms;
        long newExpiration = System.currentTimeMillis() + renewDuration;
        player2expiration.put(player.getGameProfile().getName(), newExpiration);

        TextHelper.sendTextByKey(player, "fake_player_manager.renew.success", ChronosUtil.toDefaultDateFormat(newExpiration));
    }

    public static void invalidFakePlayers() {
        player2fakePlayers.values()
            .forEach(value -> value.removeIf(fakePlayerName -> {
                ServerPlayerEntity fakePlayer = ServerHelper.getOnlinePlayerByNameIgnoreCase(fakePlayerName);
                return fakePlayer == null || fakePlayer.isRemoved();
            }));
    }

    public static boolean canSpawnFakePlayer(@NotNull ServerPlayerEntity player) {
        /* check */
        int capsLimit = computeFakePlayerCapsLimit();
        int currentQuantity = player2fakePlayers.computeIfAbsent(player.getGameProfile().getName(), k -> new ArrayList<>()).size();
        return currentQuantity < capsLimit;
    }

    public static void addMyFakePlayer(@NotNull ServerPlayerEntity player, @NotNull String fakePlayer) {
        player2fakePlayers.computeIfAbsent(player.getGameProfile().getName(), k -> new ArrayList<>())
            .add(fakePlayer);
    }

    public static boolean isMyFakePlayer(@NotNull ServerPlayerEntity player, @NotNull String fakePlayer) {
        List<String> myFakePlayers = player2fakePlayers.computeIfAbsent(player.getGameProfile().getName(), k -> new ArrayList<>());
        return myFakePlayers
            .stream()
            // The `carpet` mod ignores the case of fake-player name.
            .anyMatch(it -> it.equalsIgnoreCase(fakePlayer));
    }

    public static boolean isMyFakePlayer(@NotNull ServerPlayerEntity player, @NotNull ServerPlayerEntity fakePlayer) {
        return isMyFakePlayer(player, fakePlayer.getGameProfile().getName());
    }

    public static boolean canManipulateFakePlayer(@NotNull CommandContext<ServerCommandSource> ctx, String fakePlayer) {
        // IMPORTANT: disable /player ... shadow command for online-player
        if (ctx.getNodes().get(2).getNode().getName().equals("shadow")) return false;

        // bypass: console
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return true;

        // bypass: op
        if (ctx.getSource().hasPermissionLevel(4)) return true;

        // check
        return isMyFakePlayer(player, fakePlayer);
    }

    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    public static int computeFakePlayerCapsLimit() {
        int currentDays = LocalDate.now().getDayOfWeek().getValue();
        LocalTime currentTime = LocalTime.now();
        int currentMinutes = currentTime.getHour() * 60 + currentTime.getMinute();

        Optional<List<Integer>> first = config.model().caps_limit_rule
            .stream()
            .filter(it -> currentDays >= it.get(0) && currentMinutes >= it.get(1))
            .findFirst();
        if (first.isPresent()) {
            return first.get().get(2);
        }

        return -1;
    }

    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ManageFakePlayersJob manageFakePlayersJob = new ManageFakePlayersJob();
            Managers.getScheduleManager().scheduleJob(manageFakePlayersJob);
        });
    }

}
