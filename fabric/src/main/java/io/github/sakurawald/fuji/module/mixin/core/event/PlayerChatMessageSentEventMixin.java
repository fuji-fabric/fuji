package io.github.sakurawald.fuji.module.mixin.core.event;

import com.google.common.collect.EvictingQueue;
import io.github.sakurawald.annotation.PhasedMixinTemplate;
import io.github.sakurawald.auxiliary.WeaverUtil;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.message.player.PlayerChatMessageSentEvent;
import java.util.Queue;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@PhasedMixinTemplate
@Mixin(value = ServerPlayNetworkHandler.class)
public abstract class PlayerChatMessageSentEventMixin {

    @Unique
    private static final Queue<Long> DUPLICATED_SENT_TEXT_FILTER = EvictingQueue.create(8);

    @Unique
    private static long toUniqueKey(@NotNull SignedMessage signedMessage) {
        // NOTE: The SignedMessage#getSalt method only works in online-mode server. In offline-mode server, it always returns 0.
        // NOTE: The hashCode() is used as the distinguish key, because the SentMessage#send is called inside a loop, and will not be modified.
        // NOTE: For chat-related mod compatibility, here I have to capture the chat message from network layer, and do the filter things. I want to ensure that's the last possible point the chat message can be processed.
        return signedMessage.hashCode();
    }

    /**
     * The injection point should be `RETURN` instead of `TAIL`.
     * Since MC 1.21.5, there is an early return used to check the signature of SignedMessage, making it not work in `offline mode`.
     */
    @EventProducer(PlayerChatMessageSentEvent.class)
    @Inject(method = "sendChatMessage", at = @At(value = "RETURN"))
    void producePlayerChatMessageSentEvent(SignedMessage signedMessage, MessageType.Parameters parameters, CallbackInfo ci) {
        /* Filter duplicated messages. */
        long uniqueKey = toUniqueKey(signedMessage);
        if (DUPLICATED_SENT_TEXT_FILTER.contains(uniqueKey)) {
            return;
        }
        DUPLICATED_SENT_TEXT_FILTER.add(uniqueKey);

        /* Produce the event. */
        PlayerChatMessageSentEvent event = new PlayerChatMessageSentEvent(signedMessage, parameters);
        EventManager.dispatchEvent(PlayerChatMessageSentEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }
}

