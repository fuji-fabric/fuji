package io.github.sakurawald.module.initializer.command_toolbox.seen;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.ChronosUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.argument.wrapper.impl.OfflinePlayerName;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.command_toolbox.seen.config.model.SeenDataModel;
import lombok.Getter;
import net.minecraft.server.command.ServerCommandSource;

public class SeenInitializer extends ModuleInitializer {

    @Getter
    private static final BaseConfigurationHandler<SeenDataModel> data = new ObjectConfigurationHandler<>("seen.json", SeenDataModel.class);

    @CommandNode("seen")
    @CommandRequirement(level = 4)
    @Document("Query the last online time of a player.")
    private static int $seen(@CommandSource ServerCommandSource source, OfflinePlayerName playerName) {
        String target = playerName.getValue();

        if (data.model().player2seen.containsKey(target)) {
            Long time = data.model().player2seen.get(target);
            TextHelper.sendMessageByKey(source, "seen.success", target, ChronosUtil.toStandardDateFormat(time));
        } else {
            TextHelper.sendMessageByKey(source, "seen.fail");
        }
        return CommandHelper.Return.SUCCESS;
    }

}
