package io.github.sakurawald.fuji.module.initializer.command_toolbox.hat;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

@Document(id = 1751972399779L, value = """
    This module provides the `/hat` command.
    And allows a player to put `any item` in the `head slot`.
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
