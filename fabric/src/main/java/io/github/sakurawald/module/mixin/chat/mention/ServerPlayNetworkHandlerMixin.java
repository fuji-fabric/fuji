package io.github.sakurawald.module.mixin.chat.mention;

import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.job.impl.MentionPlayersJob;
import io.github.sakurawald.module.initializer.chat.mention.ChatMentionInitializer;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 1000 + 750)
public abstract class ServerPlayNetworkHandlerMixin {

    @Unique
    private List<ServerPlayerEntity> resolveMentionedPlayers(String string) {
        /* make list */
        String[] playerNames = ServerHelper.getServer().getPlayerNames();
        List<ServerPlayerEntity> list = Arrays.stream(playerNames)
            .filter(string::contains)
            // mention the longest name first
            .sorted(Comparator.comparingInt(String::length).reversed())
            .map(ServerHelper::getPlayerByName)
            .toList();

        /* submit list */
        if (!list.isEmpty()) {
            MentionPlayersJob.requestJob(ChatMentionInitializer.config.model().mention_player, list);
        }

        return list;
    }

    @Unique
    private String replaceMentionString(@NotNull String string) {
        List<ServerPlayerEntity> mentionedPlayers = resolveMentionedPlayers(string);
        for (ServerPlayerEntity mentionedPlayer : mentionedPlayers) {
            String playerName = mentionedPlayer.getGameProfile().getName();
            String replacement = ChatMentionInitializer.config.model().mention_format.formatted(playerName);
            string = string.replace(playerName, replacement);
        }

        return string;
    }

    @ModifyVariable(method = "onChatMessage", at = @At(value = "HEAD"), argsOnly = true)
    public ChatMessageC2SPacket modifyChatMessageSentByPlayers(ChatMessageC2SPacket original) {
        String oldChatMessage = original.chatMessage();
        String newChatMessage = replaceMentionString(oldChatMessage);

        return new ChatMessageC2SPacket(newChatMessage, original.timestamp(), original.comp_947(), original.comp_948(), original.acknowledgment());
    }

}
