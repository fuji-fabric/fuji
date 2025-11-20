package mod.fuji.module.initializer.command_toolbox.lore;

import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.ItemStackHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.List;

@ColorBox(id = 1751972580951L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Set lore for item in mainhand.
    Issue: `/lore set \\<rainbow\\>the first line\\<newline\\>\\<bold\\>\\<green\\>the second`
    """)


@CommandNode("lore")
@CommandRequirement(level = 4)
public class LoreInitializer extends ModuleInitializer {

    @Document(id =1751825438214L, value = "Clear all lore in item.")
    @CommandNode("unset")
    private static int $unset(@CommandSource CommandSourceStack source) {
        return CommandHelper.Pattern.withItemInMainHandCommand(source, (player, stack) -> {
            ItemStackHelper.Lore.setLore(stack, List.of());
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1751825447182L, value = "Set lore for item.")
    @CommandNode("set")
    private static int $set(@CommandSource CommandSourceStack source, GreedyString lore) {
        return CommandHelper.Pattern.withItemInMainHandCommand(source, (player, stack) -> {
            List<Component> texts = TextHelper.getTextListByValue(player, lore.getValue());
            ItemStackHelper.Lore.setLore(stack, texts);
            return CommandHelper.Return.SUCCESS;
        });
    }
}
