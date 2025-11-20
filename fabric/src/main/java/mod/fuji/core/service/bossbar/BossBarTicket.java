package mod.fuji.core.service.bossbar;

import mod.fuji.core.service.bossbar.command.argument.wrapper.StepType;
import lombok.Data;
import lombok.Setter;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
public abstract class BossBarTicket {

    // the type of ticks should be `float`, instead of `int`.
    final float totalTicks;
    final float stepTicksPerTick;
    final ServerBossEvent bossBar;
    final StepType stepType;

    @Setter
    boolean aborted;


    public BossBarTicket(ServerBossEvent bossBar, int totalMs, StepType stepType, @NotNull List<ServerPlayer> players) {
        this.bossBar = bossBar;
        this.totalTicks = 20 * ((float) totalMs / 1000);
        this.stepType = stepType;

        // compute fields
        this.bossBar.setProgress(this.computeInitialProgress());
        this.stepTicksPerTick = this.computeStepTicksPerTick();

        // add players for this bossbar
        players.forEach(this::addPlayer);

        // hide the bossbar, so that if the ticket is aborted in the first tick, will not be rendered.
        this.bossBar.setVisible(false);
    }

    public BossBarTicket(ServerBossEvent bossBar, int totalMs, @NotNull List<ServerPlayer> players) {
        this(bossBar, totalMs, StepType.FORWARD, players);
    }

    private float computeInitialProgress() {
        return this.stepType == StepType.FORWARD ? 0f : 1f;
    }

    private float computeStepTicksPerTick() {
        float abs = 1F / this.totalTicks;
        return this.stepType == StepType.FORWARD ? abs : -abs;
    }

    public @NotNull Collection<ServerPlayer> getPlayers() {
        return Collections.unmodifiableCollection(this.bossBar.getPlayers());
    }

    public void step() {
        if (this.stepType == StepType.FORWARD) {
            this.progress(Math.min(1f, this.progress() + this.stepTicksPerTick));
        } else {
            this.progress(Math.max(0f, this.progress() + this.stepTicksPerTick));
        }
    }

    public boolean isCompleted() {
        if (this.stepType == StepType.FORWARD) {
            return Float.compare(this.progress(), 1f) == 0;
        } else {
            return Float.compare(this.progress(), 0f) == 0;
        }
    }

    public float progress() {
        return this.bossBar.getProgress();
    }

    public void progress(float progress) {
        this.bossBar.setProgress(progress);
    }

    public void addPlayer(@NotNull ServerPlayer player) {
        this.bossBar.addPlayer(player);
    }

    public void removePlayer(@NotNull ServerPlayer player) {
        this.bossBar.removePlayer(player);
    }

    public void clearPlayers() {
        this.bossBar.setVisible(false);
        this.bossBar.removeAllPlayers();
    }

    @SuppressWarnings({"EmptyMethod", "unused"})
    protected void onPlayerDisconnected(ServerPlayer player) {
        // no-op
    }

    protected boolean preProgressChange() {
        // Returns abort this ticket?
        return true;
    }

    protected boolean postProgressChange() {
        // Returns abort this ticket?
        return true;
    }

    protected abstract void onComplete();

}
