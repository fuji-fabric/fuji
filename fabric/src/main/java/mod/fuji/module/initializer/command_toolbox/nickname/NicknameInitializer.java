package mod.fuji.module.initializer.command_toolbox.nickname;

import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.ModifyPlayerDisplayNameEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.command_toolbox.nickname.config.model.NicknameConfigModel;
import mod.fuji.module.initializer.command_toolbox.nickname.config.model.NicknameDataModel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@Document(id = 1758032261807L, value = """
    This module allows customization of the `display name` of a player.
    """)
@ColorBox(id = 1758032281065L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ Using the `nickname` module with `Styled Chat` mod.
    See: https://github.com/fuji-fabric/fuji/issues/489
    """)
@CommandNode("nickname")
public class NicknameInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<NicknameDataModel> data = ObjectConfigurationHandler.ofModule("nickname.json", NicknameDataModel.class);

    public static final BaseConfigurationHandler<NicknameConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, NicknameConfigModel.class);

    private static String formatNickname(@NotNull ServerPlayer player, @NotNull String inputNickName) {
        // Parse the placeholders first to make the Java Formatter happy.
        String nicknameFormat = config.model().getNicknameFormat();
        nicknameFormat = TextHelper.Operators.getString(TextHelper.getTextByValue(player, nicknameFormat));

        inputNickName = config.model().getNicknameConstraints().apply(inputNickName, player);
        return nicknameFormat.formatted(inputNickName);
    }

    @Document(id = 1751825221904L, value = "Set the display name.")
    @CommandNode("set")
    private static int $set(@CommandSource @CommandTarget ServerPlayer player, GreedyString format) {
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
    private static int $reset(@CommandSource @CommandTarget ServerPlayer player) {
        String playerName = PlayerHelper.getPlayerName(player);

        data.model().format.player2format.remove(playerName);
        data.writeStorage();
        PlayerHelper.updateDisplayName(player);

        TextHelper.sendTextByKey(player, "nickname.unset");
        return CommandHelper.Return.SUCCESS;
    }

    @EventConsumer
    private static void modifyPlayerDisplayName(ModifyPlayerDisplayNameEvent event) {
        Player player = event.getPlayer();
        String playerName = PlayerHelper.getPlayerName(player);
        String preferredNicknameFormat = NicknameInitializer.data.model().format.player2format.get(playerName);
        if (preferredNicknameFormat != null) {
            Component newValue = TextHelper.getTextByValue(null, preferredNicknameFormat);
            event.setText(newValue);
        }
    }
}
