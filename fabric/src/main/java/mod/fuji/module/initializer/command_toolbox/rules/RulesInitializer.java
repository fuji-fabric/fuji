package mod.fuji.module.initializer.command_toolbox.rules;

import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.service.paged_text.PagedMessageText;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.command_toolbox.rules.config.RulesConfigModel;
import net.minecraft.server.level.ServerPlayer;

@ColorBox(id = 1753331899534L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Ensure the players are allowed to use `/command-callback` command.
    The `/command-callback` command is a fuji command, used for `click event`.
    In vanilla Minecraft, if a player has `no permission` to use that command, the client will says `Unknown Command` error.
    """)
public class RulesInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<RulesConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, RulesConfigModel.class);

    @Document(id = 1751825371097L, value = "Query the server rules.")
    @CommandNode("rules")
    private static int $rules(@CommandSource @CommandTarget ServerPlayer player) {
        String string = config.model().rules;

        PagedMessageText pagedMessageText = new PagedMessageText(player, string);
        pagedMessageText.sendPage(player, 0);
        return CommandHelper.Return.SUCCESS;
    }

}
