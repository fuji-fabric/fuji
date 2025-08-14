package io.github.sakurawald.fuji.module.initializer.kit;

import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.kit.command.argument.wrapper.KitName;
import io.github.sakurawald.fuji.module.initializer.kit.gui.KitEditorGui;
import io.github.sakurawald.fuji.module.initializer.kit.service.KitService;
import io.github.sakurawald.fuji.module.initializer.kit.structure.Kit;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

@Document(id = 1751824812720L, value = """
    Make a set of `item stacks` as a `kit`, and give the kit to players.
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
@ColorBox(id = 1751977848415L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Claim a specific kit automatically for online players.
    You can use `command_scheduler` module, to execute the `kit claim command` for online players automatically every minute.
    """)
@ColorBox(id = 1751977880532L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Give the new-bie player a kit.
    You can use `command_event` module, to give a kit to the new-bie player.
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
        /* Verify. */
        String kitName = kit.getValue();
        if (!KitService.hasKit(kitName)) {
            TextHelper.sendTextByKey(source, "kit.kit.empty");
            return CommandHelper.Return.FAILURE;
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
