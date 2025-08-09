package io.github.sakurawald.fuji.module.initializer.echo.send_actionbar;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;

@Document(id = 1751975960555L, value = """
    This module provides `/send-actionbar` command.
    To send the `text` as `action bar` to a specified player.
    """)
@ColorBox(id = 1751976088392L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Send a title to a player.
    Issue: `/send-title Alice --mainTitle "\\<rainbow\\>Hello" --subTitle "\\<blue\\>World" --fadeInTicks 60 --stayTicks 60 --fadeOutTicks 60`

    ◉ Send a title to online players.
    Issue: `/foreach send-title %player:name% --mainTitle "\\<rainbow\\>Hello %player:name%"`
    """)
public class SendActionbarInitializer extends ModuleInitializer {

    @CommandNode("send-actionbar")
    @CommandRequirement(level = 4)
    private static int $sendActionBar(ServerPlayerEntity player, GreedyString rest) {
        player.sendMessage(TextHelper.getTextByValue(player, rest.getValue()), true);
        return CommandHelper.Return.SUCCESS;
    }

}
