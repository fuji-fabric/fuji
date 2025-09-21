package mod.fuji.module.initializer.command_toolbox.trashcan;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class TrashCanInitializer extends ModuleInitializer {

    @Document(id = 1751825455610L, value = "Open a trans can gui.")
    @CommandNode("trashcan")
    private static int $trashcan(@CommandSource @CommandTarget ServerPlayerEntity player) {
        int rows = 3;
        SimpleInventory simpleInventory = new SimpleInventory(rows * 9);

        Text titleText = TextHelper.getTextByKey(player, "trashcan.gui.title");
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory((i, inventory, p) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, i, inventory, simpleInventory, rows), titleText));
        return CommandHelper.Return.SUCCESS;
    }
}
