package io.github.sakurawald.fuji.module.initializer.chat.mention;

import io.github.sakurawald.fuji.core.auxiliary.StringUtil;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.player.OnPlayerChatMessageEvent;
import io.github.sakurawald.fuji.core.job.impl.PlaySoundJob;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.chat.mention.config.model.ChatMentionConfigModel;
import java.util.Optional;
import java.util.regex.Pattern;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

@Document(id = 1751826738578L, value = """
    This module allows you to mention another online player in chat:
    1. The target player name will be highlighted.
    2. The target player will be sound notified.
    """)
@ColorBox(id = 1751870571897L, color = ColorBox.ColorBoxTypes.WARNING, value = """
    ◉ This module work partially with `Styled Chat` mod.
    You can use this module with that mod.
    It works, but you will not get the `mention player` rendered in chat.
    Other things like the `sound notify` will still work.
    """)
public class ChatMentionInitializer extends ModuleInitializer {
    private static final BaseConfigurationHandler<ChatMentionConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, ChatMentionConfigModel.class);

    private static List<ServerPlayerEntity> resolveMentionedOnlinePlayers(@NotNull String chatString) {
        /* Resolve mentioned online players. */
        return PlayerHelper.Lookup.getOnlinePlayerNames()
            .stream()
            .filter(playerName -> chatString.endsWith(playerName) || chatString.contains(playerName + StringUtil.SPACE))
            // Mention the longest name first.
            .sorted(Comparator.comparingInt(String::length).reversed())
            .map(PlayerHelper.Lookup::getOnlinePlayerByName)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    }

    private static void submitMentionPlayersJob(@NotNull List<ServerPlayerEntity> mentionedPlayers) {
        /* Submit the mention player job. */
        if (!mentionedPlayers.isEmpty()) {
            LogUtil.debug("Submit new mention job: mentionedPlayers = {}", mentionedPlayers.stream().map(PlayerHelper::getPlayerName).toList());
            PlaySoundJob.scheduleJob(config.model().getMentionPlayer(), mentionedPlayers);
        }
    }

    public static @NotNull Text replaceMentionText(@NotNull Text text) {
        /* Resolve mentioned player names. */
        String chatString = TextHelper.Operators.getString(text);
        List<ServerPlayerEntity> mentionedPlayers = resolveMentionedOnlinePlayers(chatString);
        submitMentionPlayersJob(mentionedPlayers);

        /* Replace the mentioned player texts. */
        for (ServerPlayerEntity mentionedPlayer : mentionedPlayers) {
            String playerName = PlayerHelper.getPlayerName(mentionedPlayer);
            String replacementString = config.model().getMentionFormat().formatted(playerName);
            Text replacementText = TextHelper.getTextByValue(mentionedPlayer, replacementString);

            // Update the oldText variable.
            text = TextHelper.Replacer.replaceTextWithRegex(text, Pattern.quote(playerName), (matcher) -> replacementText);
        }

        return text;
    }

    @EventConsumer(injectorPriority = EventConsumer.HIGHEST, consumerPriority = EventConsumer.LOWER)
    private static void handleOnPlayerChatEvent(OnPlayerChatMessageEvent event) {
        /* Get signed message. */
        SignedMessage signedMessage = event.getSignedMessage();

        /* Replace the text. */
        Text oldValue = signedMessage.getContent();
        Text newValue = ChatMentionInitializer.replaceMentionText(oldValue);
        SignedMessage newSignedMessage = signedMessage.withUnsignedContent(newValue);
        event.setSignedMessage(newSignedMessage);
    }

}
