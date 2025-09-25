package mod.fuji.module.initializer.command_attachment.job;

import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.auxiliary.minecraft.UuidHelper;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.job.abst.FixedIntervalJob;
import mod.fuji.core.manager.Managers;
import mod.fuji.module.initializer.command_attachment.CommandAttachmentInitializer;
import mod.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import mod.fuji.module.initializer.command_attachment.service.CommandAttachmentService;
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
public class TestSteppingOnBlockJob extends FixedIntervalJob {

    private static final Map<String, String> player2lastSteppingBlockUUID = new HashMap<>();

    public TestSteppingOnBlockJob() {
        super(null, null, null, intervalSupplier(), REPEAT_INDEFINITELY, false);
    }

    private static void scheduleJob() {
        TestSteppingOnBlockJob job = new TestSteppingOnBlockJob();
        Managers.getScheduleManager().addJob(job);
    }

    public static void reloadJob() {
        Managers.getScheduleManager().deleteJobs(TestSteppingOnBlockJob.class);
        scheduleJob();
    }

    private static int intervalSupplier() {
        return CommandAttachmentInitializer.config.model().getTestSteppingOnBlockIntervalInMillSeconds();
    }

    private static void testSteppingBlockForPlayer(@NotNull ServerPlayerEntity player) {
        String playerName = PlayerHelper.getPlayerName(player);
        String lastSteppingBlockUUID = player2lastSteppingBlockUUID.get(playerName);
        ServerWorld serverWorld = EntityHelper.getServerWorld(player);
        String currentSteppingBlockUUID = UuidHelper.getAttachedUuid(serverWorld, player.getSteppingPos());

        /* Ignore the trigger if last stepping block is the same. */
        if (currentSteppingBlockUUID.equals(lastSteppingBlockUUID)) return;

        /* Update last stepping block, and execute attached commands. */
        player2lastSteppingBlockUUID.put(playerName, currentSteppingBlockUUID);

        /* Trigger it. */
        ServerHelper.executeSync(() -> CommandAttachmentService.tryTriggerAttachmentDataNode(currentSteppingBlockUUID, player, List.of(InteractType.STEP_ON), () -> {}));
    }

    private static void testSteppingBlockForPlayers() {
        PlayerHelper.Lookup
            .getOnlinePlayers()
            .forEach(TestSteppingOnBlockJob::testSteppingBlockForPlayer);
    }

    @Override
    public void execute(JobExecutionContext context) {
        testSteppingBlockForPlayers();
    }
}
