package io.github.sakurawald.module.mixin.chat.history;

import io.github.sakurawald.module.initializer.chat.history.ChatHistoryInitializer;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
#if MC_VER <= MC_1_20_1
#elif MC_VER > MC_1_20_1
import net.minecraft.server.network.ConnectedClientData;
import org.jetbrains.annotations.NotNull;
#endif

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// NOTE: Use a lower priority, to ensure the chat history is re-played before other welcome messages.
@Mixin(value = PlayerManager.class, priority = 1000 - 500)
public class SendChatHistoryMixin {

    #if MC_VER <= MC_1_20_1
    @Inject(at = @At(value = "TAIL"), method = "onPlayerConnect")
    void sendChatHistoryToNewJoinedPlayer(ClientConnection clientConnection, ServerPlayerEntity player, CallbackInfo ci)
    #elif MC_VER > MC_1_20_1
    @Inject(at = @At(value = "TAIL"), method = "onPlayerConnect")
    void sendChatHistoryToNewJoinedPlayer(ClientConnection connection, @NotNull ServerPlayerEntity player, ConnectedClientData commonListenerCookie, CallbackInfo ci)
    #endif
    {
        ChatHistoryInitializer.replayChatHistory(player);
    }

}
