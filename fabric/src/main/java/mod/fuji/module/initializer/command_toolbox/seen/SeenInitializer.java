package mod.fuji.module.initializer.command_toolbox.seen;

import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.ChronosUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.OfflinePlayerName;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.PlayerLeftEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.command_toolbox.seen.config.model.SeenDataModel;
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
            return CommandHelper.Return.SUCCESS;
        } else {
            TextHelper.sendTextByKey(source, "seen.fail");
            return CommandHelper.Return.FAILURE;
        }
    }

    @EventConsumer
    private static void onPlayerLeft(PlayerLeftEvent event) {
        String playerName = PlayerHelper.getPlayerName(event.getPlayer());
        SeenInitializer.getData().model().player2seen.put(playerName, System.currentTimeMillis());
        SeenInitializer.getData().writeStorage();
    }
}
