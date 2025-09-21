package mod.fuji.module.initializer.kit;

import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.kit.command.argument.wrapper.KitName;
import mod.fuji.module.initializer.kit.gui.KitEditorGui;
import mod.fuji.module.initializer.kit.gui.KitPreviewGui;
import mod.fuji.module.initializer.kit.service.KitService;
import mod.fuji.module.initializer.kit.structure.Kit;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

@Document(id = 1751824812720L, value = """
    This module allows creating a set of item stacks as a `kit` and distributing the kit to players.
    """)
@ColorBox(id = 1751977591928L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Create a kit.
    Issue: `/kit editor` to create a new `kit`.

    ◉ Give a kit to a player.
    Issue: `/kit give Alice \\<kit-name\\>` to give a `kit` to a player.

    ◉ Create a user-level command, for players to `claim a kit`.
    <red>The `/kit give` command is a `admin-level` command, you should NOT allow players to use it directly.
    <green>TIP: You need to use `command_bundle` module to create a new `user-level` command, for players to `claim a kit`.
    """)
@ColorBox(id = 1751977756034L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Associate a cooldown to a kit.
    You can use `command_cooldown` module, to create a `named cooldown` for the `kit claim command`.

    To create a `named cooldown`
    Issue: `/command-cooldown create example-kit-cooldown 60000`

    To test the cooldown, and give the kit if the cooldown is satisfied.
    Issue: `/command-cooldown test example-kit-cooldown Alice --onFailed "send-message %player:name% wait a moment" kit give %player:name% example-kit|send-message %player:name% kit received.`
    """)
@ColorBox(id = 1751977848415L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Claim a specific kit automatically for online players.
    You can use `command_scheduler` module, to execute the `kit claim command` for online players automatically every minute.
    """)
@ColorBox(id = 1751977880532L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Give the new-bie player a kit.
    You can use `command_event` module, to give a kit to the new-bie player.
    """)
@ColorBox(id = 1756110650928L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Make a GUI to display all available kits.
    You can integrate with the `command_menu` module.
    To create a `menu` to `display` available kits, making it easier to `claim` a `kit`.

    ◉ Attach the kit claim command to objects.
    You can integrate with `command_attachment` module.
    To attach the kit claim command to a `block`, `entity` or `item`.
    """)
@TestCase(action = "Create a new kit using `/kit editor` command.", targets = "See if the `kit editor` works.")
@TestCase(action = "Give the new kit using `/kit give` command.", targets = "See if the items is inserted in the proper slots. (Note that the player in creative mode can always pick up the same items even their inventory is full.)")


@CommandNode("kit")
@CommandRequirement(level = 4)
public class KitInitializer extends ModuleInitializer {

    @Document(id = 1751824817401L, value = "Open the kit editor GUI.")
    @CommandNode("editor")
    private static int $editor(@CommandSource ServerPlayerEntity player) {
        KitEditorGui.make(player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751824821391L, value = "Give the kit to the player.")
    @CommandNode("give")
    private static int $give(@CommandSource ServerCommandSource source, ServerPlayerEntity player, KitName kit) {
        Kit kitInstance = KitService.readKit(kit.getValue());
        KitService.giveKit(player, kitInstance);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1756372814981L, value = "Open a GUI to pre-view the specified kit.")
    @CommandNode("preview")
    private static int $preview(@CommandSource ServerCommandSource source, ServerPlayerEntity player, KitName kit) {
        Kit $kit = KitService.readKit(kit.getValue());
        KitPreviewGui
            .make(player, $kit)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Override
    protected void onInitialize() {
        KitService.createKitDirectory();
    }

}
