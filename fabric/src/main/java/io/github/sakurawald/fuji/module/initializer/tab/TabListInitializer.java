package io.github.sakurawald.fuji.module.initializer.tab;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.tab.config.model.TabListConfigModel;
import io.github.sakurawald.fuji.module.initializer.tab.job.RenderHeaderAndFooterJob;

@Document(id = 1751826913154L, value = """
    Customize the TAB list.
    """)
@ColorBox(id = 1751980365809L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    ◉ I want a complex `tab` list.
    If you want to design a complex and advanced `tab` list.
    I would recommend to use https://github.com/NEZNAMY/TAB

    ◉ I want to customize the `scoreboard`.
    Use the mod mentioned above. It works perfect.
    """)
public class TabListInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<TabListConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, TabListConfigModel.class);

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

}
