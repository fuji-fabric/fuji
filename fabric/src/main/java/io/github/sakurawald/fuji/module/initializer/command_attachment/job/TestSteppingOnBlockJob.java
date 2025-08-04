package io.github.sakurawald.fuji.module.initializer.command_attachment.job;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.UuidHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.job.abst.CronJob;
import io.github.sakurawald.fuji.core.manager.impl.scheduler.ScheduleManager;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.service.CommandAttachmentService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobExecutionContext;

@Document(id = 1751826425009L, value = """
    This `job` is used to test if the player is stepping on a `block` with `attached commands`.
    """)
public class TestSteppingOnBlockJob extends CronJob {

    private static final Map<String, String> player2lastSteppingBlockUUID = new HashMap<>();

    public TestSteppingOnBlockJob() {
        super(() -> ScheduleManager.CRON_EVERY_SECOND);
    }

    public static void testSteppingBlockForPlayer(@NotNull ServerPlayerEntity player) {
        String playerName = PlayerHelper.getPlayerName(player);
        String lastSteppingBlockUUID = player2lastSteppingBlockUUID.get(playerName);
        ServerWorld serverWorld = EntityHelper.getServerWorld(player);
        String currentSteppingBlockUUID = UuidHelper.getAttachedUuid(serverWorld, player.getSteppingPos());

        /* Ignore the trigger if last stepping block is the same. */
        if (currentSteppingBlockUUID.equals(lastSteppingBlockUUID)) return;

        /* Update last stepping block, and execute attached commands. */
        player2lastSteppingBlockUUID.put(playerName, currentSteppingBlockUUID);

        /* Trigger it. */
        ServerHelper.executeSync(() -> CommandAttachmentService.tryTriggerAttachmentModel(currentSteppingBlockUUID, player, List.of(InteractType.STEP_ON), () -> {}));
    }

    public static void testSteppingBlockForPlayers() {
        PlayerHelper.Lookup.getOnlinePlayers().forEach(TestSteppingOnBlockJob::testSteppingBlockForPlayer);
    }

    @Override
    public void execute(JobExecutionContext context) {
        testSteppingBlockForPlayers();
    }
}
