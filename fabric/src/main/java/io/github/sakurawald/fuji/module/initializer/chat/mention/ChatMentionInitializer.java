package io.github.sakurawald.fuji.module.initializer.chat.mention;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.job.impl.PlaySoundJob;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.chat.mention.config.model.ChatMentionConfigModel;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Document(id = 1751826738578L, value = """
    This module allows you to mention another online player in chat:
    1. The target player name will be highlighted.
    2. The target player will be sound notified.
    """)

@ColorBox(id = 1751870571897L, color = ColorBox.ColorBlockTypes.WARNING, value = """
    ◉ This module work partially with `Styled Chat` mod.
    You can use this module with that mod.
    It works, but you will not get the `mention player` rendered in chat.
    Other things like the `sound notify` will still work.
    """)

public class ChatMentionInitializer extends ModuleInitializer {
    private static final BaseConfigurationHandler<ChatMentionConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, ChatMentionConfigModel.class);

    private static List<ServerPlayerEntity> resolveMentionedOnlinePlayers(String chatString) {
        /* Resolve mentioned online players. */
        String[] onlinePlayerNames = ServerHelper.getServer().getPlayerNames();
        List<ServerPlayerEntity> mentionedPlayers = Arrays.stream(onlinePlayerNames)
            .filter(chatString::contains)
            // Mention the longest name first.
            .sorted(Comparator.comparingInt(String::length).reversed())
            .map(PlayerHelper::getOnlinePlayerByName)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

        /* Submit the mention player job. */
        if (!mentionedPlayers.isEmpty()) {
            LogUtil.debug("Submit new mention job: mentionedPlayers = {}", mentionedPlayers.stream().map(PlayerHelper::getPlayerName).toList());
            PlaySoundJob.scheduleJob(config.model().mention_player, mentionedPlayers);
        }

        return mentionedPlayers;
    }

    public static Text replaceMentionText(@NotNull Text original) {
        /* Resolve mentioned player names. */
        String chatString = TextHelper.Operators.visitString(original);
        List<ServerPlayerEntity> mentionedPlayers = resolveMentionedOnlinePlayers(chatString);

        /* Replace the mentioned player texts. */
        for (ServerPlayerEntity mentionedPlayer : mentionedPlayers) {
            String playerName = mentionedPlayer.getGameProfile().getName();
            String replacementString = config.model().mention_format.formatted(playerName);
            Text replacementText = TextHelper.getTextByValue(mentionedPlayer, replacementString);

            // Re-assign the value of original.
            original = TextHelper.Operators.replaceTextWithRegex(original, playerName, () -> replacementText);
        }

        return original;
    }
}
