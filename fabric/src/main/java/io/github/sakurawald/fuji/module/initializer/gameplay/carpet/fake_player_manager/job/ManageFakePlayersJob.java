package io.github.sakurawald.fuji.module.initializer.gameplay.carpet.fake_player_manager.job;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.job.abst.CronJob;
import io.github.sakurawald.fuji.core.manager.impl.scheduler.ScheduleManager;
import io.github.sakurawald.fuji.module.initializer.gameplay.carpet.fake_player_manager.service.FakePlayerManagerService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobExecutionContext;

@Document(id = 1751827017263L, value = """
    This `job` is used to check the `fake-player caps limit` for each player.
    """)
public class ManageFakePlayersJob extends CronJob {

    public ManageFakePlayersJob() {
        super(() -> ScheduleManager.CRON_EVERY_MINUTE);
    }

    @Override
    public void execute(@NotNull JobExecutionContext context) {
        checkCapsLimit();
    }

    private static void checkCapsLimit() {
        /* Remove invalid entries first. */
        removeInvalidFakePlayerNameEntries();

        /* Update the values. */
        int capsLimit = FakePlayerManagerService.getFakePlayerCapsLimit();
        long currentTimeMs = System.currentTimeMillis();

        FakePlayerManagerService
            .player2fakePlayers
            .entrySet()
            .forEach(entry -> {
                String ownerPlayerName = entry.getKey();

                /* Make the new value. */
                long expirationTimeOfTheOwnerPlayer = FakePlayerManagerService.player2expiration.computeIfAbsent(ownerPlayerName, k -> -1L);

                final Integer[] allowedFakePlayersCount = {0};

                List<String> newValue = entry
                    .getValue()
                    .stream()
                    .filter(fakePlayerName -> {
                        /* If a fake player is offline, then remove it from the tracked names. */
                        Optional<ServerPlayerEntity> fakePlayer = PlayerHelper.getOnlinePlayerByNameIgnoreCase(fakePlayerName);
                        if (fakePlayer.isEmpty()) {
                            return false;
                        }
                        ServerPlayerEntity $fakePlayer = fakePlayer.get();

                        /* If the expiration time of the owner player is exceeded, then kill all of its fake players. */
                        if (currentTimeMs >= expirationTimeOfTheOwnerPlayer) {
                            /* Auto-renew the fake players if the owner player is online. */
                            Optional<ServerPlayerEntity> ownerPlayer = PlayerHelper.getOnlinePlayerByNameIgnoreCase(ownerPlayerName);
                            if (ownerPlayer.isPresent()) {
                                FakePlayerManagerService.renewMyFakePlayers(ownerPlayer.get());
                                return true;
                            }

                            /* Kill all of its fake players due to expiration time. */
                            EntityHelper.killEntity($fakePlayer);
                            TextHelper.sendBroadcastByKey("fake_player_manager.kick_for_expiration", PlayerHelper.getPlayerName($fakePlayer), ownerPlayerName);
                            return false;
                        }

                        /* If the caps limit is exceeded, kill the newest fake player first. */
                        if (allowedFakePlayersCount[0] < capsLimit) {
                            allowedFakePlayersCount[0]++;
                            return true;
                        } else {
                            EntityHelper.killEntity($fakePlayer);
                            TextHelper.sendBroadcastByKey("fake_player_manager.kick_for_amount", PlayerHelper.getPlayerName($fakePlayer), ownerPlayerName);
                            return false;
                        }

                    })
                    .collect(Collectors.toList());

                /* Set the new value. */
                entry.setValue(newValue);
            });

    }

    private static void removeInvalidFakePlayerNameEntries() {
        FakePlayerManagerService.player2fakePlayers
            .values()
            .forEach(value -> value.removeIf(fakePlayerName -> {
                Optional<ServerPlayerEntity> fakePlayer = PlayerHelper.getOnlinePlayerByNameIgnoreCase(fakePlayerName);
                return fakePlayer.isEmpty() || fakePlayer.get().isRemoved();
            }));
    }

}
