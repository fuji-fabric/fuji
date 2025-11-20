package mod.fuji.module.initializer.command_toolbox.reply;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;
import java.util.HashMap;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;


@Document(id = 1751972433657L, value = """
    This module provides the `/reply` command.
    To reply the player who recently `/msg` or `/tell` you.
    """)
@ColorBox(id = 1751972495394L, color = ColorBox.ColorBoxTypes.TIP, value = """
    You can use `command_alias` module, to create a new command `/r` as the alias to `/reply` command.
    """)
public class ReplyInitializer extends ModuleInitializer {

    private static final HashMap<String, String> player2replyTargetPlayer = new HashMap<>();

    public static void setReplyTarget(@NotNull String player, @NotNull String target) {
        player2replyTargetPlayer.put(player, target);
    }

    @Document(id = 1751825375878L, value = "Reply the player who recently /msg or /tell you.")
    @CommandNode("reply")
    private static int $reply(@CommandSource ServerPlayer player, GreedyString message) {
        String sourcePlayerName = PlayerHelper.getPlayerName(player);
        String targetPlayerName = player2replyTargetPlayer.get(sourcePlayerName);

        try {
            CommandHelper
                .getCommandDispatcher()
                .execute("msg %s %s".formatted(targetPlayerName, message.getValue()), player.createCommandSourceStack());
            return CommandHelper.Return.SUCCESS;
        } catch (CommandSyntaxException e) {
            TextHelper.sendTextByKey(player, "reply.no_target");
            return CommandHelper.Return.FAILURE;
        }
    }

    @Document(id = 1756134707146L, value = "Set the reply target player.")
    @CommandNode("reply set-target")
    @CommandRequirement(level = 4)
    private static int $setReplyTarget(@CommandSource ServerPlayer source, ServerPlayer target) {
        String sourceName = PlayerHelper.getPlayerName(source);
        String targetName = PlayerHelper.getPlayerName(target);
        setReplyTarget(sourceName, targetName);
        return CommandHelper.Return.SUCCESS;
    }

}
