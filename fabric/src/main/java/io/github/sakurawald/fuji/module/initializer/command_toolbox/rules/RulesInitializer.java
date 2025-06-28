package io.github.sakurawald.fuji.module.initializer.command_toolbox.rules;

import io.github.sakurawald.fuji.core.annotation.Document;
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

public class RulesInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<RulesConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, RulesConfigModel.class);

    @Document("Query the server rules.")
    @CommandNode("rules")
    private static int asMessage(@CommandSource @CommandTarget ServerPlayerEntity player) {
        String string = config.model().rules;

        PagedMessageText pagedMessageText = new PagedMessageText(player, string);
        pagedMessageText.sendPage(player, 0);
        return CommandHelper.Return.SUCCESS;
    }

}
