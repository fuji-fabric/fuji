package io.github.sakurawald.core.manager.impl.bossbar;

import io.github.sakurawald.core.command.argument.wrapper.StepType;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
public abstract class BossBarTicket {

    // the type of ticks should be `float`, instead of `int`.
    final float totalTicks;
    final float stepTicksPerTick;
    final ServerBossBar bossBar;
    final StepType stepType;

    @Setter
    boolean aborted;


    public BossBarTicket(ServerBossBar bossBar, int totalMs, StepType stepType, @NotNull List<ServerPlayerEntity> players) {
        this.bossBar = bossBar;
        this.totalTicks = 20 * ((float) totalMs / 1000);
        this.stepType = stepType;

        // compute fields
        this.bossBar.setPercent(this.computeInitialProgress());
        this.stepTicksPerTick = this.computeStepTicksPerTick();

        // add players for this bossbar
        players.forEach(this::addPlayer);

        // hide the bossbar, so that if the ticket is aborted in the first tick, will not be rendered.
        this.bossBar.setVisible(false);
    }

    public BossBarTicket(ServerBossBar bossBar, int totalMs, @NotNull List<ServerPlayerEntity> players) {
        this(bossBar, totalMs, StepType.FORWARD, players);
    }

    private float computeInitialProgress() {
        return this.stepType == StepType.FORWARD ? 0f : 1f;
    }

    private float computeStepTicksPerTick() {
        float abs = 1F / this.totalTicks;
        return this.stepType == StepType.FORWARD ? abs : -abs;
    }

    public @NotNull Collection<ServerPlayerEntity> getPlayers() {
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
        return this.bossBar.getPercent();
    }

    public void progress(float progress) {
        this.bossBar.setPercent(progress);
    }

    public void addPlayer(@NotNull ServerPlayerEntity player) {
        this.bossBar.addPlayer(player);
    }

    public void removePlayer(@NotNull ServerPlayerEntity player) {
        this.bossBar.removePlayer(player);
    }

    public void clearPlayers() {
        this.bossBar.setVisible(false);
        this.bossBar.clearPlayers();
    }

    @SuppressWarnings({"EmptyMethod", "unused"})
    protected void onPlayerDisconnected(ServerPlayerEntity player) {
        // no-op
    }

    /***
     * @return abort this ticket
     */
    protected boolean preProgressChange() {
        return true;
    }

    /***
     * @return abort this ticket
     */
    protected boolean postProgressChange() {
        return true;
    }

    protected abstract void onComplete();

}
