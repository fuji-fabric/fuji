package mod.fuji.module.initializer.echo.send_bossbar.structure;

import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.service.bossbar.BossBarManager;
import mod.fuji.core.service.bossbar.command.argument.wrapper.StepType;
import mod.fuji.core.service.bossbar.BossBarTicket;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SendBossbarTicket extends BossBarTicket {

    final ServerPlayer player;
    final String title;
    final Runnable onComplete;
    final int totalSecond = (int) (getTotalTicks() / 20);
    float elapsedTicks;

    public SendBossbarTicket(String title, BossEvent.BossBarColor color, BossEvent.BossBarOverlay style, int totalMS, StepType stepType, @NotNull ServerPlayer player, Runnable onComplete) {
        super(BossBarManager.makeServerBossEvent(Component.empty(), color, style), totalMS, stepType, List.of(player));
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
        Component title = TextHelper.getTextByValue(this.player, timeStr);
        this.getBossBar().setName(title);

        this.elapsedTicks++;
        return true;
    }

    @Override
    protected void onComplete() {
        this.onComplete.run();
    }
}
