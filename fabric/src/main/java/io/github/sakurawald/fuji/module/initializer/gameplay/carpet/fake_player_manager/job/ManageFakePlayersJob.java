package io.github.sakurawald.fuji.module.initializer.gameplay.carpet.fake_player_manager.job;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.job.abst.CronJob;
import io.github.sakurawald.fuji.core.manager.impl.scheduler.ScheduleManager;
import io.github.sakurawald.fuji.module.initializer.gameplay.carpet.fake_player_manager.service.FakePlayerManagerService;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobExecutionContext;

@Document(id = 1751827017263L, value = """
    This `job` is used to check the `fake-player limits` for each player.
    """)
public class ManageFakePlayersJob extends CronJob {

    public ManageFakePlayersJob() {
        super(() -> ScheduleManager.CRON_EVERY_MINUTE);
    }

    public static void checkCapsLimit() {
        /* invalid */
        invalidFakePlayers();

        /* update value */
        int capsLimit = FakePlayerManagerService.getFakePlayerCapsLimit();
        long currentTimeMs = System.currentTimeMillis();

        FakePlayerManagerService.player2fakePlayers.entrySet()
            .forEach(e -> {
                String ownerPlayerName = e.getKey();

                /* make new value */
                long expiration = FakePlayerManagerService.player2expiration.computeIfAbsent(ownerPlayerName, k -> 0L);
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
                                FakePlayerManagerService.renewMyFakePlayers(owner);
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

    public static void invalidFakePlayers() {
        FakePlayerManagerService.player2fakePlayers.values()
            .forEach(value -> value.removeIf(fakePlayerName -> {
                ServerPlayerEntity fakePlayer = ServerHelper.getOnlinePlayerByNameIgnoreCase(fakePlayerName);
                return fakePlayer == null || fakePlayer.isRemoved();
            }));
    }

    @Override
    public void execute(@NotNull JobExecutionContext context) {
        checkCapsLimit();
    }

}
