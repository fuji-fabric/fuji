package io.github.sakurawald.module.mixin.chat.spy;

import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.module.initializer.chat.spy.ChatSpyInitializer;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Unique
    private static String recentlyContentString = "";

    @Shadow
    public abstract ServerPlayerEntity getPlayer();

    @Inject(method = "sendChatMessage", at = @At(value = "HEAD"))
    public void spy(SignedMessage signedMessage, MessageType.Parameters parameters, CallbackInfo ci) {
        /* filter -> whitelist message types */
        String messageType = parameters.type().getIdAsString();
        if (ChatSpyInitializer.config.model().message_type.whitelist.stream().noneMatch(it -> it.matches(messageType))) {
            return;
        }

        /* make spy text */
        Text content = parameters.applyChatDecoration(signedMessage.getContent());
        String contentString = content.getString();

        if (ChatSpyInitializer.config.model().ignore_consecutive_same_text && contentString.equals(recentlyContentString)) {
            return;
        }
        recentlyContentString = contentString;

        Text receiver = getPlayer().getDisplayName();
        MutableText spyText = Text.empty();
        spyText.append(content)
            .append(TextHelper.TEXT_SPACE)
            .append(TextHelper.getTextByKey(null, "chat.spy.indicator"))
            .append(TextHelper.TEXT_SPACE)
            .append(receiver);

        /* log console */
        if (ChatSpyInitializer.config.model().log_console) {
            LogUtil.info(spyText.getString());
        }

        /* send spy text */
        ServerHelper.getPlayers()
            .stream()
            .filter(it -> ChatSpyInitializer.getOptions(it).enabled)
            .forEach(it -> it.sendMessage(spyText));
    }
}
