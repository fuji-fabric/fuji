package mod.fuji.module.initializer.command_toolbox.hat;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

@Document(id = 1751972399779L, value = """
    This module provides the `/hat` command.
    It allows a player to put `any item` in the `head equipment slot`.
    """)
public class HatInitializer extends ModuleInitializer {

    @Document(id = 1751825381383L, value = "Wear the item in hand.")
    @CommandNode("hat")
    private static int $hat(@CommandSource @CommandTarget ServerPlayer player) {
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack headSlotItem = player.getItemBySlot(EquipmentSlot.HEAD);

        player.setItemSlot(EquipmentSlot.HEAD, mainHandItem);
        player.setItemInHand(InteractionHand.MAIN_HAND, headSlotItem);
        TextHelper.sendTextByKey(player, "hat.success");
        return CommandHelper.Return.SUCCESS;
    }

}
