package mod.fuji.module.mixin.core.event;

import com.google.common.collect.EvictingQueue;
import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.player.PlayerChatMessageSentEvent;
import java.util.Objects;
import java.util.Queue;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@PhasedMixinTemplate
@Mixin(value = ServerGamePacketListenerImpl.class)
public abstract class PlayerChatMessageSentEventMixin {

    @Shadow
    public abstract ServerPlayer getPlayer();

    @Unique
    private static final Queue<Long> DUPLICATED_SENT_TEXT_FILTER = EvictingQueue.create(8);

    @Unique
    private static long toUniqueKey(@NotNull PlayerChatMessage signedMessage, ChatType.Bound parameters) {
        // NOTE: The SignedMessage#getSalt method only works in online-mode server. In offline-mode server, it always returns 0.
        // NOTE: The hashCode() is used as the distinguish key, because the SentMessage#send is called inside a loop, and will not be modified.
        // NOTE: For chat-related mod compatibility, here I have to capture the chat message from network layer, and do the filter things. I want to ensure that's the last possible point the chat message can be processed.
        String messageTypeString = RegistryHelper.getIdAsString(parameters);

        return Objects.hash(signedMessage.hashCode(), messageTypeString.hashCode());
    }

    /**
     * The injection point should be `RETURN` instead of `TAIL`.
     * Since MC 1.21.5, there is an early return used to check the signature of SignedMessage, making it not work in `offline mode`.
     */
    @EventProducer(PlayerChatMessageSentEvent.class)
    @Inject(method = "sendPlayerChatMessage", at = @At(value = "RETURN"))
    void producePlayerChatMessageSentEvent(PlayerChatMessage signedMessage, ChatType.Bound parameters, CallbackInfo ci) {
        /* Filter duplicated messages. */
        long uniqueKey = toUniqueKey(signedMessage, parameters);
        if (DUPLICATED_SENT_TEXT_FILTER.contains(uniqueKey)) {
            return;
        }
        DUPLICATED_SENT_TEXT_FILTER.add(uniqueKey);

        /* Produce the event. */
        ServerPlayer receiverPlayer = getPlayer();
        PlayerChatMessageSentEvent event = new PlayerChatMessageSentEvent(receiverPlayer, signedMessage, parameters);
        EventManager.dispatchEvent(PlayerChatMessageSentEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }
}

