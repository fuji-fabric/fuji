package mod.fuji.core.service.game_task.structure;

import lombok.Data;

@Data
public class GameTask {
    int remainingRunTicks;
    Runnable onStart;
    Runnable onTick;
    Runnable onEnd;
    boolean started = false;
    boolean completed = false;

    public GameTask(int remainingRunTicks) {
        this(remainingRunTicks, () -> {}, () -> {}, () -> {});
    }

    public GameTask(int remainingRunTicks, Runnable onTick) {
        this(remainingRunTicks, onTick, () -> {}, () -> {});
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
            this.completed = true;
        }
    }

    public void decreaseRemainingRunTicks() {
        this.remainingRunTicks--;
    }

}
