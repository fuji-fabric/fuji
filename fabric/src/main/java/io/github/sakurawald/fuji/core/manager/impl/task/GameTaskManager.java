package io.github.sakurawald.fuji.core.manager.impl.task;

import io.github.sakurawald.fuji.core.event.impl.ServerTickEvents;
import io.github.sakurawald.fuji.core.manager.abst.BaseManager;
import io.github.sakurawald.fuji.core.manager.impl.task.structure.GameTask;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public class GameTaskManager extends BaseManager {

    private final CopyOnWriteArrayList<GameTask> tasks = new CopyOnWriteArrayList<>();

    @Override
    public void onInitialize() {
        ServerTickEvents.START_SERVER_TICK.register(this::onGameTick);
    }

    private void onGameTick(@NotNull MinecraftServer server) {
        /* Run tasks in server thread. */
        tasks.forEach(GameTask::onTick);

        /* Remove completed tasks. */
        tasks.removeIf(GameTask::isCompleted);
    }

    public void submitTask(@NotNull GameTask task) {
        this.tasks.add(task);
    }

    public void submitTasks(@NotNull Collection<GameTask> tasks) {
        this.tasks.addAll(tasks);
    }


}
