package io.github.sakurawald.fuji.module.initializer.command_toolbox.reply;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Objects;


@Document(id = 1751972433657L, value = """
    This module provides the `/reply` command.
    To reply the player who recently `/msg` or `/tell` you.
    """)
@ColorBox(id = 1751972495394L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    You can use `command_alias` module, to create a new command `/r` as the alias to `/reply` command.
    """)
public class ReplyInitializer extends ModuleInitializer {

    private static final HashMap<String, String> player2replyTargetPlayer = new HashMap<>();

    public static void setReplyTarget(String player, String target) {
        player2replyTargetPlayer.put(player, target);
    }

    @Document(id = 1751825375878L, value = "Reply the player who recently /msg or /tell you.")
    @CommandNode("reply")
    private static int $reply(@CommandSource ServerPlayerEntity player, GreedyString message) {
        String target = player2replyTargetPlayer.get(player.getGameProfile().getName());

        try {
            Objects.requireNonNull(CommandHelper.getCommandDispatcher())
                .execute("msg %s %s".formatted(target, message.getValue()), player.getCommandSource());
        } catch (CommandSyntaxException e) {
            TextHelper.sendTextByKey(player, "reply.no_target");
        }

        return CommandHelper.Return.SUCCESS;
    }

}
