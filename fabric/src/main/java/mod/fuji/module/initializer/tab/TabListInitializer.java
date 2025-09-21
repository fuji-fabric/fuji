package mod.fuji.module.initializer.tab;

import mod.fuji.core.auxiliary.RandomUtil;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.ModifyPlayerListNameEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.tab.config.model.TabListConfigModel;
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
