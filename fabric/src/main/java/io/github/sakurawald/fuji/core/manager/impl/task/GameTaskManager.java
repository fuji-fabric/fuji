package io.github.sakurawald.fuji.core.manager.impl.task;

import io.github.sakurawald.fuji.core.annotation.Unused;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.server.tick.ServerTickStartEvent;
import io.github.sakurawald.fuji.core.manager.abst.BaseManager;
import io.github.sakurawald.fuji.core.manager.impl.task.structure.GameTask;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jetbrains.annotations.NotNull;

public class GameTaskManager extends BaseManager {

    private static final CopyOnWriteArrayList<GameTask> TASK_QUEUE = new CopyOnWriteArrayList<>();

    @Override
    public void onInitialize() {}

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
