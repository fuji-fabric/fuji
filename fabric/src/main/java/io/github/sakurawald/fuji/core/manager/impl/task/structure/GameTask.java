package io.github.sakurawald.fuji.core.manager.impl.task.structure;

import lombok.Data;

@Data
public class GameTask {
    int remainingRunTicks;
    Runnable onStart;
    Runnable onTick;
    Runnable onEnd;
    boolean started = false;

    public GameTask(int remainingRunTicks, Runnable runnable) {
        this(remainingRunTicks, runnable, () -> {}, () -> {});
    }

    public GameTask(int remainingRunTicks, Runnable onTick, Runnable onStart, Runnable onEnd) {
        this.remainingRunTicks = remainingRunTicks;
        this.onStart = onStart;
        this.onTick = onTick;
        this.onEnd = onEnd;
    }

    public void onTick() {
        this.decreaseRemainingRunTicks();

        if (!this.started) {
            this.started = true;
            this.onStart.run();
        }

        this.getOnTick().run();

        if (this.remainingRunTicks <= 0) {
            this.onEnd.run();
        }
    }

    public void decreaseRemainingRunTicks() {
        this.remainingRunTicks--;
    }

    public boolean isCompleted() {
        return this.remainingRunTicks == 0;
    }

}
