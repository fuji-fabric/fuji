package mod.fuji.core.job.impl;

import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.job.abst.FixedIntervalJob;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;
import mod.fuji.core.job.JobManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor
public class PlaySoundJob extends FixedIntervalJob {

    public PlaySoundJob(JobDataMap jobDataMap, int intervalMs, int repeatCount) {
        super(null, null, jobDataMap, () -> intervalMs, repeatCount, false);
    }

    public static void scheduleJob(PlaySoundJobSetup setup, List<ServerPlayerEntity> mentionedPlayers) {
        /* Make the job. */
        int intervalMs = setup.interval_ms;
        int repeatCount = setup.repeat_count;
        PlaySoundJob mentionPlayersJob = new PlaySoundJob(new JobDataMap() {
            {
                this.put(List.class.getName(), mentionedPlayers);
                this.put(PlaySoundJobSetup.class.getName(), setup);
            }
        }, intervalMs, repeatCount);

        /* Schedule the job. */
        JobManager.addJob(mentionPlayersJob);
    }

    public static void scheduleJob(PlaySoundJobSetup setup, ServerPlayerEntity serverPlayer) {
        scheduleJob(setup, new ArrayList<>(Collections.singletonList(serverPlayer)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(@NotNull JobExecutionContext context) {
        List<ServerPlayerEntity> players = (List<ServerPlayerEntity>) context.getJobDetail().getJobDataMap().get(List.class.getName());
        PlaySoundJobSetup setup = (PlaySoundJobSetup) context.getJobDetail().getJobDataMap().get(PlaySoundJobSetup.class.getName());

        players
            .stream()
            .filter(Objects::nonNull)
            .forEach(player -> {
                // NOTE: The playSound should be called in main thread.
                ServerHelper.executeSync(() -> {
                    SoundEvent soundEvent = SoundEvent.of(RegistryHelper.makeIdentifierOrThrow(setup.sound));
                    SoundCategory soundCategory = SoundCategory.BLOCKS;
                    PlayerHelper.playSound(player, soundEvent, soundCategory, setup.volume, setup.pitch);
                });
            });

    }

    @Data
    @NoArgsConstructor
    public static class PlaySoundJobSetup {
        @Document(id = 1751823822772L, value = "The `sound` identifier.")
        public @NotNull String sound = "entity.experience_orb.pickup";

        public float volume = 100f;
        public float pitch = 1f;

        @Document(id = 1751823830414L, value = "The times to play this sound.")
        public int repeat_count = 3;

        @Document(id = 1751823834755L, value = "The interval between each sound play.")
        public int interval_ms = 1000;
    }
}
