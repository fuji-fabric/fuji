package io.github.sakurawald.fuji.module.initializer.command_toolbox.more;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;


public class MoreInitializer extends ModuleInitializer {

    @Document(id = 1751825196038L, value = "Set the count of item in hand to max count.")
    @CommandNode("more")
    @CommandRequirement(level = 4)
    private static int $more(@CommandSource ServerCommandSource source) {
        return CommandHelper.Pattern.withItemInMainHand(source, (player, itemStack) -> {
            itemStack.setCount(itemStack.getMaxCount());
            return CommandHelper.Return.SUCCESS;
        });
    }

}
