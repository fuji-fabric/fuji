package io.github.sakurawald.fuji.module.initializer.view;

import io.github.sakurawald.fuji.core.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.OfflinePlayerName;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.view.gui.EnderChestRedirectScreenFactory;
import io.github.sakurawald.fuji.module.initializer.view.gui.InventoryRedirectScreenFactory;
import net.minecraft.server.network.ServerPlayerEntity;

@Document("""
    Allow you to edit player's inventory and enderchest.
    """)
@CommandNode("view")
@CommandRequirement(level = 4)
public class ViewInitializer extends ModuleInitializer {

    private static void checkSelfView(ServerPlayerEntity source, OfflinePlayerName target) {
        if (source.getGameProfile().getName().equals(target.getValue())) {
            TextHelper.sendMessageByKey(source, "view.failed.self_view");
            throw new AbortCommandExecutionException();
        }
    }

    @Document("View the player's inventory.")
    @CommandNode("inv")
    private static int inv(@CommandSource ServerPlayerEntity source, OfflinePlayerName target) {
        checkSelfView(source, target);

        source.openHandledScreen(new InventoryRedirectScreenFactory(source, target.getValue()).makeFactory());
        return CommandHelper.Return.SUCCESS;
    }

    @Document("View the player's enderchest.")
    @CommandNode("ender")
    private static int ender(@CommandSource ServerPlayerEntity source, OfflinePlayerName target) {
        checkSelfView(source, target);

        source.openHandledScreen(new EnderChestRedirectScreenFactory(source, target.getValue()).makeFactory());
        return CommandHelper.Return.SUCCESS;
    }
}
