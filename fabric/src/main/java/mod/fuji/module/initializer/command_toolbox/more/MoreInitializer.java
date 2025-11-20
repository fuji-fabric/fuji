package mod.fuji.module.initializer.command_toolbox.more;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.module.initializer.ModuleInitializer;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;


public class MoreInitializer extends ModuleInitializer {

    @Document(id = 1751825196038L, value = "Set the count of item in hand to max count.")
    @CommandNode("more")
    @CommandRequirement(level = 4)
    private static int $more(@CommandSource CommandSourceStack source, Optional<Boolean> oversize) {
        return CommandHelper.Pattern.withItemInMainHandCommand(source, (player, itemStack) -> {
            int newCount = oversize
                .filter(it -> it)
                .map(it -> 64)
                .orElseGet(itemStack::getMaxStackSize);

            itemStack.setCount(newCount);
            return CommandHelper.Return.SUCCESS;
        });
    }

}
