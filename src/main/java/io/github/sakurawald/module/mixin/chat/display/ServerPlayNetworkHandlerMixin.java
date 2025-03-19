package io.github.sakurawald.module.mixin.chat.display;

import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.module.initializer.chat.display.ChatDisplayInitializer;
import io.github.sakurawald.module.initializer.chat.display.helper.DisplayHelper;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 1000 + 500)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Unique
    private Text replaceDisplayText(Text original, ServerPlayerEntity sharedByPlayer) {
        MutableText newValue
            = TextHelper.replaceTextWithRegex(original
            , ChatDisplayInitializer.config.model().replace_pattern.item_display
            , () -> DisplayHelper.createItemDisplayText(sharedByPlayer));

        newValue
            = TextHelper.replaceTextWithRegex(newValue
            , ChatDisplayInitializer.config.model().replace_pattern.inv_display
            , () -> DisplayHelper.createInvDisplayText(sharedByPlayer));

        newValue
            = TextHelper.replaceTextWithRegex(newValue
            , ChatDisplayInitializer.config.model().replace_pattern.ender_display
            , () -> DisplayHelper.createEnderDisplayText(sharedByPlayer));
        return newValue;
    }

    @ModifyArgs(method = "handleDecoratedMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V"))
    public void modifyChatMessageSentByPlayers(Args args) {
        /* get args */
        SignedMessage signedMessage = args.get(0);

        /* make content text */
        Text contentText = replaceDisplayText(signedMessage.getContent(), player);
        args.set(0, signedMessage.withUnsignedContent(contentText));
    }
}
