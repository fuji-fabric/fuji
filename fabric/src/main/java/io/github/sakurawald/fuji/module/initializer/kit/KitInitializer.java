package io.github.sakurawald.fuji.module.initializer.kit;

import io.github.sakurawald.fuji.core.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.kit.command.argument.wrapper.KitName;
import io.github.sakurawald.fuji.module.initializer.kit.gui.KitEditorGui;
import io.github.sakurawald.fuji.module.initializer.kit.service.KitService;
import io.github.sakurawald.fuji.module.initializer.kit.structure.Kit;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

@Document("""
    Make a set of items as kit, and give the kit to players.
    """)
@CommandNode("kit")
@CommandRequirement(level = 4)
public class KitInitializer extends ModuleInitializer {

    @Document("Open the kit editor GUI.")
    @CommandNode("editor")
    private static int $editor(@CommandSource ServerPlayerEntity player) {
        List<Kit> kits = KitService.readKits();
        new KitEditorGui(player, kits, 0).open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Give the kit to the player.")
    @CommandNode("give")
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
