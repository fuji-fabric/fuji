package mod.fuji.module.initializer.echo.send_title;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Optional;

@Document(id = 1751975985135L, value = """
    This module provides `/send-title` command.
    To send the `text` as `title` to a specified player.
    """)
@ColorBox(id = 1751976416056L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Send a title to a player.
    Issue: `/send-title Alice --mainTitle "\\<rainbow\\>Hello" --subTitle "\\<blue\\>World" --fadeInTicks 60 --stayTicks 60 --fadeOutTicks 60`

    ◉ Send a title to online players.
    Issue: `/foreach send-title %player:name% --mainTitle "\\<rainbow\\>Hello %player:name%"`
    """)
@TestCase(action = "Issue the command `/send-title @s --mainTitle \"<rainbow>Hello\" --subTitle \"<blue>World\" --fadeInTicks 60 --stayTicks 60 --fadeOutTicks 60`", targets = "Consecutive optional argument should work.")
public class SendTitleInitializer extends ModuleInitializer {

    @CommandNode("send-title")
    @CommandRequirement(level = 4)
    private static int $sendTitle(@CommandSource ServerCommandSource source, ServerPlayerEntity player
        , Optional<String> mainTitle
        , Optional<String> subTitle
        , Optional<Integer> fadeInTicks
        , Optional<Integer> stayTicks
        , Optional<Integer> fadeOutTicks
    ) {

        String $mainTitle = mainTitle.orElse("");
        String $subTitle = subTitle.orElse("");
        int $fadeInTicks = fadeInTicks.orElse(10);
        int $stayTicks = stayTicks.orElse(70);
        int $fadeOutTicks = fadeOutTicks.orElse(20);

        Text mainTitleText = TextHelper.getTextByValue(player, $mainTitle);
        Text subTitleText = TextHelper.getTextByValue(player, $subTitle);

        TextHelper.Sender.sendTitleToServerPlayerEntity(player, $fadeInTicks, $stayTicks, $fadeOutTicks, mainTitleText, subTitleText);
        return CommandHelper.Return.SUCCESS;
    }

}
