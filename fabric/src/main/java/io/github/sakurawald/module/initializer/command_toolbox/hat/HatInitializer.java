package io.github.sakurawald.module.initializer.command_toolbox.hat;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.annotation.CommandTarget;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;


public class HatInitializer extends ModuleInitializer {

    @CommandNode("hat")
    @Document("Wear the item in hand.")
    private static int $hat(@CommandSource @CommandTarget ServerPlayerEntity player) {
        ItemStack mainHandItem = player.getMainHandStack();
        ItemStack headSlotItem = player.getEquippedStack(EquipmentSlot.HEAD);

        player.equipStack(EquipmentSlot.HEAD, mainHandItem);
        player.setStackInHand(Hand.MAIN_HAND, headSlotItem);
        TextHelper.sendMessageByKey(player, "hat.success");
        return CommandHelper.Return.SUCCESS;
    }

}
