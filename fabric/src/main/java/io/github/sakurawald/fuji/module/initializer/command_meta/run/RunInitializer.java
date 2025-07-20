package io.github.sakurawald.fuji.module.initializer.command_meta.run;

import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

@Document(id = 1751823988812L, value = """
    Provides `/run` command, to run a command with context.
    """)
@ColorBox(id = 1752982945496L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    ◉ What is the difference between `/run` and `/execute` command?
    1. The main purpose of `/run` command is to `modify the command context` while executing a `command` instance.
    2. The `/run` command will parse `placeholders` like `%player:name%`. (However, you can still use vanilla target selector, but NOT recommended)
    3. The `/run as {player/fake-op/console}` allows you to switch the `role` of the `command source` easily.

    ◉ A `command` is executed with a `command context`.
    In internal Minecraft, when you submit a `command` to the `command executor`. You need to provide a `command context`.
    A `command context` contains important information like:
    1. `the command source`
    2. `the value of command arguments`
    3. `the executing dimension`
    4. `the execution position`
    5. `the executing permission level`
    And more.

    ◉ The `/run` command can be used to switch the `command context`.
    You can use `/run as player` to modify the `command source` to another player.
    If player `Alice` issues the `/run as player Bob say I am %player:name%`.
    Then the `initializing command source` is `Alice`, and `executing command source` is `Bob`.
    We will report `command exception` to both the `initializing command source` and `executing command source`.
    We will use player `Bob` as the `contextual player` to `parse placeholders`.
    So the result is: The player `Bob` executes the `/say I am Bob` command.

    ◉ How the `/run as fake-op` works?
    When you issue `/run as fake-op Alice say Hi`, the `/run` command will modify the `command context`.
    To make the `command executor` treat the player `Alice` as if he has a `level permission` of `4`.

    The effect is like: We `/op` the player `Alice`, and the console executes `/run as player Alice say Hi`, then we `/deop` the player `Alice`.
    However, we didn't actually `/op` the player `Alice`.
    What we do is simple, we just tell the `command executor` to treat the player `Alice` as if he has a `level permission` of `4` when executing `this command` instance.
    """)
@ColorBox(id = 1751968631536L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Execute a command as a specified player.
    Issue: `/run as player \\<player\\> back`

    ◉ Execute a command as a fake-op.
    Issue: `/run as fake-op \\<player\\> give %player:name% minecraft:apple 1`

    ◉ Give random amount of diamonds to online players.
    Issue: `/run as console give @a minecraft:diamond %fuji:random 8 32%`

    ◉ Give online players random amount of diamonds.
    Issue: `/run as console foreach give %fuji:escape player:name% minecraft:diamond %fuji:escape fuji:random 8 32 1%`
    """)



@CommandNode("run")
@CommandRequirement(level = 4)
public class RunInitializer extends ModuleInitializer {

    @Document(id = 1751823993461L, value = "Execute a command as console.")
    @CommandNode("as console")
    private static int $runAsConsole(@CommandSource ServerCommandSource source, GreedyString rest) {
        CommandExecutor.execute(ExtendedCommandSource.asConsole(source), rest.getValue());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751823999061L, value = "Execute a command as a player.")
    @CommandNode("as player")
    private static int $runAsPlayer(@CommandSource ServerCommandSource source, ServerPlayerEntity player, GreedyString rest) {
        return CommandExecutor.execute(ExtendedCommandSource.asPlayer(source, player), rest.getValue());
    }

    @Document(id = 1751824003937L, value = "Execute a command as a player with fake-op.")
    @CommandNode("as fake-op")
    private static int $runAsFakeOp(@CommandSource ServerCommandSource source, ServerPlayerEntity player, GreedyString rest) {
        return CommandExecutor.execute(ExtendedCommandSource.asFakeOp(source, player), rest.getValue());
    }
}
