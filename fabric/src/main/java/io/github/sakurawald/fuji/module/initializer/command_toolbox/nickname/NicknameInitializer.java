package io.github.sakurawald.fuji.module.initializer.command_toolbox.nickname;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_toolbox.nickname.config.model.NicknameConfigModel;
import io.github.sakurawald.fuji.module.initializer.command_toolbox.nickname.config.model.NicknameDataModel;
import lombok.Getter;
import net.minecraft.server.network.ServerPlayerEntity;

@CommandNode("nickname")
public class NicknameInitializer extends ModuleInitializer {

    @Getter
    private static final BaseConfigurationHandler<NicknameDataModel> data = new ObjectConfigurationHandler<>("nickname.json", NicknameDataModel.class);

    private static final BaseConfigurationHandler<NicknameConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, NicknameConfigModel.class);

    private static String formatNickname(String string) {
        return config.model().transform_nickname.formatted(string);
    }

    @Document(id = 1751825221904L, value = "Set the display name.")
    @CommandNode("set")
    private static int $set(@CommandSource @CommandTarget ServerPlayerEntity player, GreedyString format) {
        String name = player.getGameProfile().getName();
        String value = format.getValue();
        value = formatNickname(value);

        data.model().format.player2format.put(name, value);
        data.writeStorage();
        PlayerHelper.updateDisplayName(player);

        TextHelper.sendTextByKey(player, "nickname.set");
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751825227207L, value = "Clear the display name.")
    @CommandNode("reset")
    private static int $reset(@CommandSource @CommandTarget ServerPlayerEntity player) {
        String name = player.getGameProfile().getName();

        data.model().format.player2format.remove(name);
        data.writeStorage();
        PlayerHelper.updateDisplayName(player);

        TextHelper.sendTextByKey(player, "nickname.unset");
        return CommandHelper.Return.SUCCESS;
    }
}
