package io.github.sakurawald.fuji.module.initializer.warning;


import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.warning.gui.WarningGui;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;

public class WarningInitializer extends ModuleInitializer {

    @Document("Open the warning GUI.")
    @CommandNode("warning")
    @CommandRequirement(level = 4)
    private static int $warning(@CommandSource ServerPlayerEntity player) {
        List<String> offlinePlayerNames = ServerHelper.getOfflinePlayerNames();
        new WarningGui(null, player, offlinePlayerNames, 0)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

}
