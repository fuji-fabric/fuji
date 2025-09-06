package io.github.sakurawald.fuji.module.initializer.tab;

import io.github.sakurawald.fuji.core.auxiliary.RandomUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.event.message.impl.on_demand.player.ModifyPlayerListNameEvent;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.tab.config.model.TabListConfigModel;
import io.github.sakurawald.fuji.module.initializer.tab.job.RenderHeaderAndFooterJob;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Document(id = 1751826913154L, value = """
    Customize the TAB list.
    """)
@ColorBox(id = 1751980365809L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ I want a complex `tab` list.
    If you want to design a complex and advanced `tab` list.
    I would recommend to use https://github.com/NEZNAMY/TAB

    ◉ I want to customize the `scoreboard`.
    Use the mod mentioned above. It works perfect.
    """)
public class TabListInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<TabListConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, TabListConfigModel.class);

    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            RenderHeaderAndFooterJob renderHeaderAndFooterJob = new RenderHeaderAndFooterJob();
            Managers.getScheduleManager().scheduleJob(renderHeaderAndFooterJob);
        });
    }

    @Override
    protected void onReload() {
        PlayerHelper.updateDisplayNames();
    }


    @EventConsumer
    private static void modifyPlayerListName(ModifyPlayerListNameEvent event) {
        // Respect other's modification.
        @Nullable Text original = event.getText();
        if (original == null) {
            ServerPlayerEntity player = event.getPlayer();
            Text newValue = TextHelper.getTextByValue(player, RandomUtil.drawList(TabListInitializer.config.model().getStyle().getBody()));
            event.setText(newValue);
        }
    }

}
