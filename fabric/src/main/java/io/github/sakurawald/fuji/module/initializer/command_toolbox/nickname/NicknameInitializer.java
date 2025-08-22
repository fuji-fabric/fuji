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
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

@CommandNode("nickname")
public class NicknameInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<NicknameDataModel> data = new ObjectConfigurationHandler<>("nickname.json", NicknameDataModel.class);

    public static final BaseConfigurationHandler<NicknameConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON_LITERAL, NicknameConfigModel.class);

    private static String formatNickname(@NotNull ServerPlayerEntity player, @NotNull String inputNickName) {
        // Parse the placeholders first to make the Java Formatter happy.
        String nicknameFormat = config.model().nicknameFormat;
        nicknameFormat = TextHelper.Operators.getString(TextHelper.getTextByValue(player, nicknameFormat));
        return nicknameFormat.formatted(inputNickName);
    }

    @Document(id = 1751825221904L, value = "Set the display name.")
    @CommandNode("set")
    private static int $set(@CommandSource @CommandTarget ServerPlayerEntity player, GreedyString format) {
        String playerName = PlayerHelper.getPlayerName(player);
        String $format = format.getValue();
        $format = formatNickname(player, $format);

        data.model().format.player2format.put(playerName, $format);
        data.writeStorage();
        PlayerHelper.updateDisplayName(player);

        TextHelper.sendTextByKey(player, "nickname.set");
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751825227207L, value = "Clear the display name.")
    @CommandNode("reset")
    private static int $reset(@CommandSource @CommandTarget ServerPlayerEntity player) {
        String playerName = PlayerHelper.getPlayerName(player);

        data.model().format.player2format.remove(playerName);
        data.writeStorage();
        PlayerHelper.updateDisplayName(player);

        TextHelper.sendTextByKey(player, "nickname.unset");
        return CommandHelper.Return.SUCCESS;
    }
}
