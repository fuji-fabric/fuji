package io.github.sakurawald.fuji.module.initializer.title;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.title.config.model.TitleConfigModel;
import io.github.sakurawald.fuji.module.initializer.title.config.model.TitleDataModel;
import io.github.sakurawald.fuji.module.initializer.title.gui.ListTitlesGui;
import io.github.sakurawald.fuji.module.initializer.title.service.TitleService;
import net.minecraft.server.network.ServerPlayerEntity;

@Document(id = 1752999308751L, value = """
    This module allows you to define `titles` to display in the chat.
    Or display the `active title` in any place that supports placeholders.
    """)
@ColorBox(id = 1753008337658L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Give the `fisher` title to player Alice.
    Issue: `/lp user Alice permission set fuji.title.obtain.fisher true`

    ◉ Give the `fisher` title to player Alice, but it expires in 7 days.
    Issue: `/lp user Alice permission settemp fuji.title.obtain.fisher true 7d`

    ◉ To display the `active title`.
    Insert the `%fuji:active_title%` in any place that supports placeholders. (Like a chat mod that supports the placeholders).
    """)

public class TitleInitializer extends ModuleInitializer {

    public static BaseConfigurationHandler<TitleConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, TitleConfigModel.class);
    public static BaseConfigurationHandler<TitleDataModel> data = ObjectConfigurationHandler.ofModule("data.json", TitleDataModel.class);

    @Document(id = 1753001051805L, value = """
        Open the `title` GUI.
        """)
    @CommandNode("title!")
    private static int $title(@CommandSource ServerPlayerEntity player) {
        ListTitlesGui
            .makeInstance(player, TitleService.getAllTitles())
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Override
    protected void registerPlaceholders() {
        TitleService.registerActiveTitlePlaceholder();
    }
}
