package mod.fuji.module.initializer.command_toolbox.itemname;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.ItemStackHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ItemNameInitializer extends ModuleInitializer {

    @CommandNode("itemname set")
    @CommandRequirement(level = 4)
    private static int $set(@CommandSource ServerPlayerEntity player, GreedyString name) {
        return CommandHelper.Pattern.withItemInMainHand(player, (item) -> {
            Text nameText = TextHelper.getTextByValue(player, name.getValue());
            ItemStackHelper.CustomName.setCustomName(item,nameText);
            return CommandHelper.Return.SUCCESS;
        });
    }

    @CommandNode("itemname reset")
    @CommandRequirement(level = 4)
    private static int $reset(@CommandSource ServerPlayerEntity player) {
        return CommandHelper.Pattern.withItemInMainHand(player, (item) -> {
            ItemStackHelper.CustomName.removeCustomName(item);
            return CommandHelper.Return.SUCCESS;
        });
    }
}
