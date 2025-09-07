package io.github.sakurawald.fuji.module.initializer.command_toolbox.seen;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.OfflinePlayerName;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.player.OnPlayerLeftEvent;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_toolbox.seen.config.model.SeenDataModel;
import lombok.Getter;
import net.minecraft.server.command.ServerCommandSource;

public class SeenInitializer extends ModuleInitializer {

    @Getter
    private static final BaseConfigurationHandler<SeenDataModel> data = ObjectConfigurationHandler.ofModule("seen.json", SeenDataModel.class);

    @Document(id = 1751825128305L, value = "Query the last online time of a player.")
    @CommandNode("seen")
    @CommandRequirement(level = 4)
    private static int $seen(@CommandSource ServerCommandSource source, OfflinePlayerName playerName) {
        String target = playerName.getValue();

        if (data.model().player2seen.containsKey(target)) {
            Long time = data.model().player2seen.get(target);
            TextHelper.sendTextByKey(source, "seen.success", target, ChronosUtil.Formatter.formatDate(time));
        } else {
            TextHelper.sendTextByKey(source, "seen.fail");
        }
        return CommandHelper.Return.SUCCESS;
    }

    @EventConsumer
    private static void onPlayerLeft(OnPlayerLeftEvent event) {
        String playerName = PlayerHelper.getPlayerName(event.getPlayer());
        SeenInitializer.getData().model().player2seen.put(playerName, System.currentTimeMillis());
        SeenInitializer.getData().writeStorage();
    }
}
