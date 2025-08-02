package io.github.sakurawald.fuji.module.initializer.command_toolbox.lore;

import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ItemStackHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;

@ColorBox(id = 1751972580951L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Set lore for item in mainhand.
    Issue: `/lore set <rainbow>the first line<newline><bold><green>the second`
    """)


@CommandNode("lore")
@CommandRequirement(level = 4)
public class LoreInitializer extends ModuleInitializer {

    @Document(id =1751825438214L, value = "Clear all lore in item.")
    @CommandNode("unset")
    private static int $unset(@CommandSource ServerCommandSource source) {
        return CommandHelper.Pattern.itemInHandCommand(source, (player, stack) -> {
            ItemStackHelper.Lore.setLore(stack, List.of());
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1751825447182L, value = "Set lore for item.")
    @CommandNode("set")
    private static int $set(@CommandSource ServerCommandSource source, GreedyString lore) {
        return CommandHelper.Pattern.itemInHandCommand(source, (player, stack) -> {
            List<Text> texts = TextHelper.getTextListByValue(player, lore.getValue());
            ItemStackHelper.Lore.setLore(stack, texts);
            return CommandHelper.Return.SUCCESS;
        });
    }
}
