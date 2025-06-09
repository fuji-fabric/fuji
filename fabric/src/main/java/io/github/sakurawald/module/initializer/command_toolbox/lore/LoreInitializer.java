package io.github.sakurawald.module.initializer.command_toolbox.lore;

import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;

@CommandNode("lore")
@CommandRequirement(level = 4)
public class LoreInitializer extends ModuleInitializer {

    @CommandNode("unset")
    @Document("Clear all lore in item.")
    private static int $unset(@CommandSource CommandContext<ServerCommandSource> ctx) {
        return CommandHelper.Pattern.itemInHandCommand(ctx, (player, item) -> {
            LoreComponent loreComponent = new LoreComponent(List.of());
            item.set(DataComponentTypes.LORE, loreComponent);
            return CommandHelper.Return.SUCCESS;
        });
    }

    @CommandNode("set")
    @Document("Set lore for item.")
    private static int $set(@CommandSource CommandContext<ServerCommandSource> ctx, GreedyString lore) {
        return CommandHelper.Pattern.itemInHandCommand(ctx, (player, item) -> {
            List<Text> texts = TextHelper.getTextListByValue(player, lore.getValue());
            LoreComponent loreComponent = new LoreComponent(texts);
            item.set(DataComponentTypes.LORE, loreComponent);
            return CommandHelper.Return.SUCCESS;
        });
    }
}
