package io.github.sakurawald.module.initializer.tab;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.RandomUtil;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.tab.config.model.TabListConfigModel;
import io.github.sakurawald.module.initializer.tab.job.RenderHeaderAndFooterJob;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

@Document("""
    Customize the TAB list.
    """)
public class TabListInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<TabListConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, TabListConfigModel.class);

    public static void render() {
        String headerControl = RandomUtil.drawList(config.model().style.header);
        String footerControl = RandomUtil.drawList(config.model().style.footer);
        for (ServerPlayerEntity player : ServerHelper.getPlayers()) {
            @NotNull Text header = TextHelper.getTextByValue(player, headerControl);
            @NotNull Text footer = TextHelper.getTextByValue(player, footerControl);
            player.networkHandler.sendPacket(new PlayerListHeaderS2CPacket(header, footer));
        }

    }

    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> new RenderHeaderAndFooterJob().schedule());
    }

    @Override
    protected void onReload() {
        ServerHelper.updateDisplayName();
    }

}
