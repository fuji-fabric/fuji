package io.github.sakurawald.module.initializer.kit;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.kit.command.argument.wrapper.KitName;
import io.github.sakurawald.module.initializer.kit.gui.KitEditorGui;
import io.github.sakurawald.module.initializer.kit.service.KitService;
import io.github.sakurawald.module.initializer.kit.structure.Kit;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

@CommandNode("kit")
@CommandRequirement(level = 4)
public class KitInitializer extends ModuleInitializer {

    @CommandNode("editor")
    @Document("Open the kit editor GUI.")
    private static int $editor(@CommandSource ServerPlayerEntity player) {
        List<Kit> kits = KitService.readKits();
        new KitEditorGui(player, kits, 0).open();
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("give")
    @Document("Give a kit to a player.")
    private static int $give(@CommandSource ServerCommandSource source, ServerPlayerEntity player, KitName kit) {
        /* Verify. */
        String kitName = kit.getValue();
        if (!KitService.hasKit(kitName)) {
            TextHelper.sendMessageByKey(source, "kit.kit.empty");
            return CommandHelper.Return.FAIL;
        }

        /* Give the kit. */
        Kit kitInstance = KitService.readKit(kitName);
        KitService.giveKit(player, kitInstance);
        return CommandHelper.Return.SUCCESS;
    }

    @Override
    protected void onInitialize() {
        KitService.createKitDirectory();
    }

}
