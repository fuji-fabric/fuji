package io.github.sakurawald.fuji.module.initializer.functional.enderchest;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;


public class EnderChestInitializer extends ModuleInitializer {

    @CommandNode("enderchest")
    @CommandRequirement(level = 4)
    private static int $enderchest(@CommandSource @CommandTarget ServerPlayerEntity player) {
        EnderChestInventory enderChestInventory = player.getEnderChestInventory();
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory((i, inventory, p) -> GenericContainerScreenHandler.createGeneric9x3(i, inventory, enderChestInventory), Text.translatable("container.enderchest")));
        player.incrementStat(Stats.OPEN_ENDERCHEST);
        return CommandHelper.Return.SUCCESS;
    }
}
