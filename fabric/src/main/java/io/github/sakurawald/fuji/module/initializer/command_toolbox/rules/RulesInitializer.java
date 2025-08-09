package io.github.sakurawald.fuji.module.initializer.command_toolbox.rules;

import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.service.paged_text.PagedMessageText;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_toolbox.rules.config.RulesConfigModel;
import net.minecraft.server.network.ServerPlayerEntity;

@ColorBox(id = 1753331899534L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ Ensure the players are allowed to use `/command-callback` command.
    The `/command-callback` command is a fuji command, used for `click event`.
    In vanilla Minecraft, if a player has `no permission` to use that command, the client will says `Unknown Command` error.
    """)
public class RulesInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<RulesConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, RulesConfigModel.class);

    @Document(id = 1751825371097L, value = "Query the server rules.")
    @CommandNode("rules")
    private static int $rules(@CommandSource @CommandTarget ServerPlayerEntity player) {
        String string = config.model().rules;

        PagedMessageText pagedMessageText = new PagedMessageText(player, string);
        pagedMessageText.sendPage(player, 0);
        return CommandHelper.Return.SUCCESS;
    }

}
