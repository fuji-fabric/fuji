package mod.fuji.core.service.game_task;

import mod.fuji.core.annotation.Unused;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.server.tick.ServerTickStartEvent;
import mod.fuji.core.service.game_task.structure.GameTask;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jetbrains.annotations.NotNull;

public class GameTaskManager {

    private static final CopyOnWriteArrayList<GameTask> TASK_QUEUE = new CopyOnWriteArrayList<>();

    public static void runInTicks(int inTicks, @NotNull Runnable runnable) {
        GameTask gameTask = new GameTask(inTicks,
            () -> {},
            () -> {},
            runnable);
        submitTask(gameTask);
    }

    @EventConsumer
    private static void tickGameTasks(@Unused ServerTickStartEvent event) {
        /* Run tasks in server thread. */
        TASK_QUEUE.forEach(GameTask::onTick);

        /* Remove completed tasks. */
        TASK_QUEUE.removeIf(GameTask::isCompleted);
    }

    public static void submitTask(@NotNull GameTask task) {
        TASK_QUEUE.add(task);
    }

    public static void submitTasks(@NotNull Collection<GameTask> tasks) {
        TASK_QUEUE.addAll(tasks);
    }


}
