package mod.fuji.module.initializer.command_bundle;

import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.annotation.Unused;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.core.document.gui.CommandsInspectionGui;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.server.command.CommandRegistrationEvent;
import mod.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.command_bundle.config.model.CommandBundleConfigModel;
import mod.fuji.module.initializer.command_bundle.service.CommandBundleService;
import mod.fuji.module.initializer.command_bundle.structure.BundleCommandDescriptor;
import net.minecraft.commands.CommandSourceStack;

@Document(id = 1751826356909L, value = """
    This module allows `creating` a new command (Called `template command` or `bundle command`):
    - `User-Defined Arguments`: the new command can accept user-defined arguments.
    - `Command Body`: the body of a new command can consist of a list of existing commands.
    - `Placeholders`: the body can support placeholder parsing.

    It can be used as a generic `command template` system.
    """)
@ColorBox(id = 1751870454656L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ The features of this module:
    1. Provide a user-friendly DSL, to create `a new custom command` easily.
    2. Support the inter-operation with `user-defined variable`, `placeholders` and `vanilla target selectors`.
    3. Support complex `argument types`: `required argument`, `literal argument` and even `optional argument with a specified default value`.
    4. A powerful `type-system`, to use the built-in `argument types`.
    5. Register and un-register `custom commands` on the fly, without a server re-start.
    """)
@ColorBox(id = 1751870456781L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ The `purpose` of this module
    This module allows you to `define` a `new command`.
    To `define` a new command, you need to specify the following things:
    1. The `pattern` of this new command: If the pattern is `claim-kit example`, then the new command is `/claim-kit example`.
    2. The `bundle` of this new command: It is the `body` of this new command. It is `a list of commands` to be executed.

    <green>To define a new `bundle command`, you need to specify the `pattern` and the `bundle` for it.
    The `pattern` describes: what does your `new command` look like?
    The `bundle` describes: what `commands` should we execute when your `new command` is executed?
    """)
@ColorBox(id = 1752892603255L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ The syntax of the `pattern`.
    The `pattern` is composed by a list of `command node`.
    For example, the `pattern` instance `first second third` describes a command `/first second third`.
    It is composed by 3 `command node`, they are all `literal arguments`.

    In the syntax of `pattern`, there are 3 types of `arguments`:
    1. `Literal Argument`: You can write it down directly. For example `first`, `second`, `third`, and `claim-kit` are all literal arguments.
    2. `Required Argument`: It's syntax is `\\<arg-type arg-name\\>`. For example, `\\<int age\\>` means a `required argument` whose `argument type is int` and `argument name is age`.
    3. `Optional Argument`: It's syntax is `[arg-type arg-name default-value]`. It is similar to `required argument`, but you can provide a `default value` if this argument is not specified by the `command source`.

    You can `reference` the value of `Required Argument` or `Optional Argument` in the `bundle` component.
    For example, you can write down `$age` to refer to a `variable` named `age` defined in the `pattern` component.

    ◉ What is the `type system` used by the syntax of `pattern`?
    Fuji will register an `argument type adapter` for a specific `argument type`.
    You can issue `/fuji inspect argument-types` to list all registered `adapters`.
    You can use any `argument type` listed in that GUI.
    """)
@ColorBox(id = 1752893166889L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ The syntax of the `bundle`.
    Actually, the `bundle` is just a `list of commands`.
    You can write `Minecraft commands` directly in the `bundle` list.

    When a `bundle command` is executed, we will execute the `list of commands` defined by `bundle` from up to down.
    Commands are executed `as console`.
    You can use `/run as player` or `/run as fake-op` to switch the command execution context, if it is needed.
    """)
@ColorBox(id = 1751870458514L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Generate powerful commands using a generator.
    You can use command generator to get powerful commands:
    https://www.gamergeeks.net/apps/minecraft/particle-command-generator
    """)
@ColorBox(id = 1751901598337L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Use a `bundle command` to combine many commands into one command.
    In this example, we want to register a new command `/composite-heal`.
    To `decorate` an existed command `/heal`.
    The decorations are:
    1. We will `say` before the execution of `/heal` command.
    2. We will spawn a `heart particle` before the execution of `/heal` command.
    3. We will `say` after the execution of `/heal` command.

    To define this `bundle command` as shown below.
    Pattern: `composite-heal`
    Bundle:
    1. `say before heal %player:name%`
    2. `run as fake-op %player:name% particle minecraft:heart ~ ~2 ~`
    3. `run as player %player:name% heal`
    4. `say after heal %player:name%`
    """)
@ColorBox(id = 1751901750629L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Use a `bundle command` to transform the form of an existed command.
    In this example, we want to register a new command `/warn`.
    As a `shortcut command` to a specific command instance.

    Pattern: `warn \\<player player-arg\\> \\<greedy greedy-arg\\>`
    Bundle:
    1. `run as player %player:name% send-message $player-arg \\<red\\>You are warned: $greedy-arg`
    """)
@ColorBox(id = 1752894328505L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Use a `bundle command` to wrap a specific command instance.
    In this example, if you want to allow players to use `/give @s minecraft:apple`.
    You only want to allow the players to get free `apples`.
    And you didn't want to allow players to use `/give` command arbitrarily.

    Then, you can define a `bundle command` like `/free-apple` to `wrap` a specific instance of `/give` command.
    Pattern: `free-apple`
    Bundle:
    1. `run as fake-op %player:name% give @s minecraft:apple`

    ◉ Define a `bundle command` to wrap a specific `/kit give` command instance.
    Pattern: `kitfood`
    Bundle:
    1. `run as fake-op %player:name% kit give @s kit-food`
    """)
@ColorBox(id = 1752895095176L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ See more advanced examples.
    The default config file contains a set of `advanced examples`.
    You can see there are many pre-defined `bundle commands`.
    Their name starts with `/my-command`.

    Besides, there are also a set of pre-defined `bundle commands` for convenience.
    For example: `/gmc`, `/gms`, `/day`, `/sun`...
    """)
@ColorBox(id = 1751983696805L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Transform the form of a command.
    - `/blocknbt` -> `/data get block`
    - `/entitynbt` -> `/data get entity`
    - `/gm`, `/gms` and `/gmc` -> `/gamemode`
    - `/findbiome` -> `/locate biome`
    - `/flyspeed` -> `/attribute Alice minecraft:generic.flying_speed`
    - `/walkspeed` -> `/attribute Alice minecraft:movement_speed`
    - `/maxhealth` -> `/attribute Alice minecraft:generic.max_health`
    - `/groundclean` -> `/kill @e[type=...]`
    - `/spawnmob` -> `/summon`
    - `/shoot` -> `/summon` with motion
    - `/smite` and `/thunder` -> `/summon minecraft:lighting_bolt`
    """)
@ColorBox(id = 1753243426623L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Define a `/tpw` command to teleport players to a specified dimension.
    Pattern: `tpw resource-world`
    Bundle:
    1. `run as fake-op %player:name% tppos --centerX 0 --centerZ 0 --maxRange 128 --dimension fuji:overworld`
    """)


@CommandNode("command-bundle")
@CommandRequirement(level = 4)
public class CommandBundleInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CommandBundleConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, CommandBundleConfigModel.class);

    @Document(id = 1751826359683L, value = "Register all commands defined in bundle-command configuration file.")
    @CommandNode("register")
    private static int $registerAllBundleCommands(@CommandSource CommandSourceStack source) {
        CommandBundleService.registerAllBundleCommands();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826362252L, value = "Un-register all bundle-commands registered in server.")
    @CommandNode("un-register")
    private static int $unregisterAllBundleCommands(@CommandSource CommandSourceStack source) {
        CommandBundleService.unregisterAllBundleCommands();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826364625L, value = "List all registered bundle-commands in server.")
    @CommandNode("list")
    private static int $list(@CommandSource CommandContext<CommandSourceStack> ctx) {
        return CommandsInspectionGui
            .inspectCommandDescriptors(ctx, it -> it instanceof BundleCommandDescriptor);
    }

    @EventConsumer
    private static void registerAllBundleCommands(@Unused ServerStartedEvent event) {
        CommandBundleService.registerAllBundleCommands();
    }

    @EventConsumer(injectorPriority = EventConsumer.HIGHER, consumerPriority = EventConsumer.HIGHER)
    private static void registerAllBundleCommands(@Unused CommandRegistrationEvent event) {
        ServerHelper.Lifecycle.ifServerInstantiated(CommandBundleService::registerAllBundleCommands);
    }

    @TestCase(action = "Issue `/reload`, `/fuji reload`, `/fuji inspect fuji-commands` and `/command-bundle list`", targets = "The bundle commands should be able to register and un-register on the fly.")
    @Override
    protected void onReload() {
        CommandBundleService.unregisterAllBundleCommands();
        CommandBundleService.registerAllBundleCommands();
    }

}
