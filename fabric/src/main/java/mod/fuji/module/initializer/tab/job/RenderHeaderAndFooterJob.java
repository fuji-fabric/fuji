package mod.fuji.module.initializer.tab.job;

import mod.fuji.core.annotation.Unused;
import mod.fuji.core.auxiliary.RandomUtil;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import mod.fuji.core.job.abst.CronJob;
import mod.fuji.core.job.JobManager;
import mod.fuji.module.initializer.tab.TabListInitializer;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobExecutionContext;

@Document(id = 1751826908172L, value = """
    This `job` is used to `update` the `header` and `footer` in `tab` list.
    """)
public class RenderHeaderAndFooterJob extends CronJob {

    public RenderHeaderAndFooterJob() {
        super(() -> TabListInitializer.config.model().getUpdateCron());
    }

    private static void updateTabList() {
        String headerFormat = RandomUtil.drawList(TabListInitializer.config.model().getStyle().getHeader());
        String footerFormat = RandomUtil.drawList(TabListInitializer.config.model().getStyle().getFooter());
        for (ServerPlayer player : PlayerHelper.Lookup.getOnlinePlayers()) {
            @NotNull Component header;
            if (TabListInitializer.config.model().getStyle().isEnableHeader()) {
                header = TextHelper.getTextByValue(player, headerFormat);
            } else {
                header = Component.empty();
            }

            @NotNull Component footer;
            if (TabListInitializer.config.model().getStyle().isEnableFooter()) {
                footer = TextHelper.getTextByValue(player, footerFormat);
            } else {
                footer = Component.empty();
            }

            player.connection.send(new ClientboundTabListPacket(header, footer));
        }

    }

    @EventConsumer
    private static void scheduleTabListRenderJob(@Unused ServerStartedEvent event) {
        RenderHeaderAndFooterJob renderHeaderAndFooterJob = new RenderHeaderAndFooterJob();
        JobManager.addJob(renderHeaderAndFooterJob);
    }

    @Override
    public void execute(JobExecutionContext context) {
        updateTabList();
    }
}
