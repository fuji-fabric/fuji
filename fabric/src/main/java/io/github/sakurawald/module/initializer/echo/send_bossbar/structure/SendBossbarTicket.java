package io.github.sakurawald.module.initializer.echo.send_bossbar.structure;

import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.argument.wrapper.StepType;
import io.github.sakurawald.core.manager.impl.bossbar.BossBarTicket;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SendBossbarTicket extends BossBarTicket {

    final ServerPlayerEntity player;
    final String title;
    final Runnable onComplete;
    final int totalSecond = (int) (getTotalTicks() / 20);
    float elapsedTicks;

    public SendBossbarTicket(String title, BossBar.Color color, BossBar.Style style, int totalMS, StepType stepType, @NotNull ServerPlayerEntity player, Runnable onComplete) {
        super(new ServerBossBar(Text.empty(), color, style), totalMS, stepType, List.of(player));
        this.player = player;
        this.title = title;
        this.onComplete = onComplete;
    }

    @Override
    protected boolean preProgressChange() {
        /* render */
        int elapsedSeconds = (int) (this.elapsedTicks / 20);
        int leftSeconds = totalSecond - elapsedSeconds;
        String timeStr = this.title
            .replace("[total_time]", String.valueOf(totalSecond))
            .replace("[elapsed_time]", String.valueOf(elapsedSeconds))
            .replace("[left_time]", String.valueOf(leftSeconds));
        Text title = TextHelper.getTextByValue(this.player, timeStr);
        this.getBossBar().setName(title);

        this.elapsedTicks++;
        return true;
    }

    @Override
    protected void onComplete() {
        this.onComplete.run();
    }
}
