package io.github.sakurawald.fuji.module.initializer.tab.job;

import io.github.sakurawald.fuji.core.auxiliary.RandomUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.job.abst.CronJob;
import io.github.sakurawald.fuji.module.initializer.tab.TabListInitializer;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
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
        for (ServerPlayerEntity player : PlayerHelper.Lookup.getOnlinePlayers()) {
            @NotNull Text header;
            if (TabListInitializer.config.model().getStyle().isEnableHeader()) {
                header = TextHelper.getTextByValue(player, headerFormat);
            } else {
                header = Text.empty();
            }

            @NotNull Text footer;
            if (TabListInitializer.config.model().getStyle().isEnableFooter()) {
                footer = TextHelper.getTextByValue(player, footerFormat);
            } else {
                footer = Text.empty();
            }

            player.networkHandler.sendPacket(new PlayerListHeaderS2CPacket(header, footer));
        }

    }

    @Override
    public void execute(JobExecutionContext context) {
        updateTabList();
    }
}
