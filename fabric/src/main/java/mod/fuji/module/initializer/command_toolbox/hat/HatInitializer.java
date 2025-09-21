package mod.fuji.module.initializer.command_toolbox.hat;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

@Document(id = 1751972399779L, value = """
    This module provides the `/hat` command.
    It allows a player to put `any item` in the `head equipment slot`.
    """)
public class HatInitializer extends ModuleInitializer {

    @Document(id = 1751825381383L, value = "Wear the item in hand.")
    @CommandNode("hat")
    private static int $hat(@CommandSource @CommandTarget ServerPlayerEntity player) {
        ItemStack mainHandItem = player.getMainHandStack();
        ItemStack headSlotItem = player.getEquippedStack(EquipmentSlot.HEAD);

        player.equipStack(EquipmentSlot.HEAD, mainHandItem);
        player.setStackInHand(Hand.MAIN_HAND, headSlotItem);
        TextHelper.sendTextByKey(player, "hat.success");
        return CommandHelper.Return.SUCCESS;
    }

}
