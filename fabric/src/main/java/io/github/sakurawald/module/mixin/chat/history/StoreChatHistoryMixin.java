package io.github.sakurawald.module.mixin.chat.history;

import com.google.common.collect.EvictingQueue;
import io.github.sakurawald.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.module.initializer.chat.history.ChatHistoryInitializer;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;

@Mixin(value = ServerPlayNetworkHandler.class)
public abstract class StoreChatHistoryMixin {

    @SuppressWarnings("UnstableApiUsage")
    @Unique
    private static final Queue<Long> DUPLICATED_SENT_TEXT_FILTER = EvictingQueue.create(10);

    @Unique
    private long getUniqueKey(SignedMessage signedMessage) {
       return signedMessage.getSalt();
    }

    @Inject(method = "sendChatMessage", at = @At(value = "TAIL"))
    void storeChatHistoryWhenSentMessage(SignedMessage signedMessage, MessageType.Parameters parameters, CallbackInfo ci) {
        /* For an identical chat message, the server will send it for all online players.
           That's why we need to filter the duplicate chat message for chat history. */
        long uniqueKey = getUniqueKey(signedMessage);
        if (!DUPLICATED_SENT_TEXT_FILTER.contains(uniqueKey)) {
            /* Filter duplicated messages. */
            DUPLICATED_SENT_TEXT_FILTER.add(uniqueKey);

            /* Filter the message by message type. */
            String messageTypeString = RegistryHelper.getMessageTypeAsString(parameters);
            if (!ChatHistoryInitializer.isMessageTypeFiltered(messageTypeString)) {
                return;
            }

            /* Reject the message by content and parameters. (Styled Chat mod will encode info into parameters, and we can detect the feature.) */
            String contentString = signedMessage.getContent().getString();
            String parametersString = parameters.toString();
            if (ChatHistoryInitializer.isMessageRejected(contentString, parametersString)) {
                return;
            }

            /* Add the message into chat history. */
            Text decoratedTextAsTheClientSideDo = parameters.applyChatDecoration(signedMessage.getContent());
            ChatHistoryInitializer.getChatHistory().add(decoratedTextAsTheClientSideDo);
        }

    }

}
