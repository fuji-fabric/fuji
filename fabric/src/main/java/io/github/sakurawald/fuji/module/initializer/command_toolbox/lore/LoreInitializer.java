package io.github.sakurawald.fuji.module.initializer.command_toolbox.lore;

import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.StackHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;

@CommandNode("lore")
@CommandRequirement(level = 4)
public class LoreInitializer extends ModuleInitializer {

    @Document("Clear all lore in item.")
    @CommandNode("unset")
    private static int $unset(@CommandSource CommandContext<ServerCommandSource> ctx) {
        return CommandHelper.Pattern.itemInHandCommand(ctx, (player, stack) -> {
            StackHelper.setLore(stack, List.of());
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document("Set lore for item.")
    @CommandNode("set")
    private static int $set(@CommandSource CommandContext<ServerCommandSource> ctx, GreedyString lore) {
        return CommandHelper.Pattern.itemInHandCommand(ctx, (player, stack) -> {
            List<Text> texts = TextHelper.getTextListByValue(player, lore.getValue());
            StackHelper.setLore(stack, texts);
            return CommandHelper.Return.SUCCESS;
        });
    }
}
