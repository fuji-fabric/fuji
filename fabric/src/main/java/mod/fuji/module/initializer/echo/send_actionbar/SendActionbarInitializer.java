package mod.fuji.module.initializer.echo.send_actionbar;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

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
        Text textByValue = TextHelper.getTextByValue(player, rest.getValue());
        TextHelper.sendText(player, textByValue, TextHelper.Sender.TextLocation.ACTION_BAR);
        return CommandHelper.Return.SUCCESS;
    }

}
