package mod.fuji.module.initializer.view;

import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.OfflinePlayerName;
import mod.fuji.core.command.exception.AbortCommandExecutionException;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.view.gui.EnderChestRedirectScreenFactory;
import mod.fuji.module.initializer.view.gui.InventoryRedirectScreenFactory;
import net.minecraft.server.network.ServerPlayerEntity;

@Document(id = 1751824970923L, value = """
    Allow you to edit a player's `slots`.

    1. Edit an online/offline player's `inventory`.
    2. Edit an online/offline player's `enderchest`.
    """)


@CommandNode("view")
@CommandRequirement(level = 4)
@TestCase(action = "Issue the `/view {inv/ender}` command on a fake-player.", targets = "You should be able to modify the slots on the fly.")
public class ViewInitializer extends ModuleInitializer {

    private static void checkSelfView(ServerPlayerEntity source, OfflinePlayerName target) {
        String sourcePlayerName = PlayerHelper.getPlayerName(source);
        String targetPlayerName = target.getValue();
        if (sourcePlayerName.equals(targetPlayerName)) {
            TextHelper.sendTextByKey(source, "view.failed.self_view");
            throw new AbortCommandExecutionException();
        }
    }

    @Document(id = 1751824976609L, value = "View the player's inventory.")
    @CommandNode("inv")
    private static int $inv(@CommandSource ServerPlayerEntity source, OfflinePlayerName target) {
        checkSelfView(source, target);

        source.openHandledScreen(new InventoryRedirectScreenFactory(source, target.getValue()).makeFactory());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751824982580L, value = "View the player's enderchest.")
    @CommandNode("ender")
    private static int $ender(@CommandSource ServerPlayerEntity source, OfflinePlayerName target) {
        checkSelfView(source, target);

        source.openHandledScreen(new EnderChestRedirectScreenFactory(source, target.getValue()).makeFactory());
        return CommandHelper.Return.SUCCESS;
    }
}
