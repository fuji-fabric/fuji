package mod.fuji.module.mixin.command_toolbox.reply;

import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.module.initializer.command_toolbox.reply.ReplyInitializer;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.commands.MsgCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(MsgCommand.class)
public class MsgCommandMixin {

    @Inject(method = "sendMessage", at = @At("HEAD"))
    private static void rememberRecentlyMessagedPlayer(@NotNull CommandSourceStack commandSourceStack, @NotNull Collection<ServerPlayer> collection, PlayerChatMessage playerChatMessage, CallbackInfo ci) {
        ServerPlayer source = commandSourceStack.getPlayer();
        if (source == null) return;

        collection.forEach(target -> {
            String targetPlayerName = PlayerHelper.getPlayerName(target);
            String sourcePlayerName = PlayerHelper.getPlayerName(source);
            ReplyInitializer.setReplyTarget(targetPlayerName, sourcePlayerName);
        });
    }
}
