package io.github.sakurawald.fuji.module.initializer.command_toolbox.trashcan;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;

public class TrashCanInitializer extends ModuleInitializer {

    @Document("Open a trans can gui.")
    @CommandNode("trashcan")
    private static int $trashcan(@CommandSource @CommandTarget ServerPlayerEntity player) {
        int rows = 3;
        SimpleInventory simpleInventory = new SimpleInventory(rows * 9);

        player.openHandledScreen(new SimpleNamedScreenHandlerFactory((i, inventory, p) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, i, inventory, simpleInventory, rows), TextHelper.getTextByKey(player, "trashcan.gui.title")));
        player.incrementStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
        return CommandHelper.Return.SUCCESS;
    }
}
