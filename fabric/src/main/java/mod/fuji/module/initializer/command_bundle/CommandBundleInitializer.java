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
    This module allows to `create` a new command. (Called `bundle command` or `template command`.)

    The new command is made up of existing commands.
    It can accept `user-defined arguments` and `placeholders`.
    This module can be used as a generic `command template` system.
    """)
@ColorBox(id = 1751870454656L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ The features of this module:
    1. Provide a user-friendly DSL, to create `a new custom command` easily.
    2. Support `user-defined variable`, `placeholders` and `vanilla target selectors`.
    3. Support complex `argument types`: `required argument`, `literal argument` and even `optional argument with a specified default value`.
    4. A powerful `type-system`, to use the built-in `argument types`.
    5. Register and un-register `custom commands` on the fly, without a server re-start.
    """)
@ColorBox(id = 1751870456781L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How to create a new command.
    <green>To create a new command, you need to specify the following things:
    1. The `head` of the command describes: What does your `new command` look like?
    2. The `body` of the command describes: What `commands` should be executed when your `new command` is executed?
    """)
@ColorBox(id = 1752892603255L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How to write the `head` component for a new command.
    The `head` is made up of `command nodes`.
    For example, the `head` instance `first second third` describes a command `/first second third`.
    It is made up of three `command nodes`, which are all `literal arguments`.

    There are 3 kinds of `arguments`:
    1. `Literal Argument`: You can write it down directly. For example, `first`, `second`, `third`, and `claim-kit` are all literal arguments.
    2. `Required Argument`: Its syntax is `\\<arg-type arg-name\\>`. For example, `\\<int age\\>` describes a `required argument` whose `argument type is int` and `argument name is age`.
    3. `Optional Argument`: Its syntax is `[arg-type arg-name default-value]`. It is similar to `required argument`, but you can provide a `default value` if this argument is not specified by the `command source`.

    You can `refer to` the value of `Required Argument` or `Optional Argument` in the `body` component.
    For example, you can write down `$age` to refer to a `variable` named `age` defined in the `head` component.

    ◉ What is the `type system` used in `head` component?
    This mod will register an `argument type adapter` for a specific `argument type`.
    You can issue `/fuji inspect argument-types` to list all registered `adapters`.
    You can use any `argument type` listed in that GUI.
    """)
@ColorBox(id = 1752893166889L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How to write the `body` component for a new command.
    The `body` component is much simple.
    It's just a list of existing commands.
    You can write any existing `Minecraft commands` directly in the `body` component.

    Besides that, you can also write `placeholders` in the `body` component.

    ◉ How do the commands in the `body` component execute?
    When a `bundle command` is executed, the commands written in the `body` component will be executed from up to down.

    All the commands are executed as console.
    If needed, you can use `/run as player` or `/run as fake-op` to switch the command execution context.

    A command will be executed `anyway` regardless of whether the previous command is executed successfully or not.
    If needed, you can use `/chain` or `/IF` to use a sequential execution model.
    """)
@ColorBox(id = 1751870458514L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Generate powerful commands using a generator.
    You can use command generator to get powerful commands:
    https://www.gamergeeks.net/apps/minecraft/particle-command-generator
    """)
@ColorBox(id = 1751901598337L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Use a `bundle command` to decorate an existing target command.
    In this example, we want to register a new command `/composite-heal`.
    To `decorate` an existing command `/heal`.
    The decorations are:
    1. It will `say` before the execution of `/heal` command.
    2. It will spawn a `heart particle` before the execution of `/heal` command.
    3. It will `say` after the execution of `/heal` command.

    Head: `composite-heal`
    Body:
    1. `say before heal %player:name%`
    2. `run as fake-op %player:name% particle minecraft:heart ~ ~2 ~`
    3. `run as player %player:name% heal`
    4. `say after heal %player:name%`
    """)
@ColorBox(id = 1751901750629L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Use a `bundle command` as a template command.
    In this example, we want to register a new command `/warn`.
    As a `template` for a specific command instance.

    Head: `warn \\<player player-arg\\> \\<greedy greedy-arg\\>`
    Body:
    1. `send-message $player-arg \\<red\\>You are warned: $greedy-arg`
    """)
@ColorBox(id = 1752894328505L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Use a `bundle command` to wrap a specific command instance.
    In this example, if you want to allow players to use `/give @s minecraft:apple`.
    You only want to allow the players to get free `apples`.
    And you didn't want to allow players to use `/give` command arbitrarily.

    Then, you can define a `bundle command` like `/free-apple` to `wrap` a specific instance of `/give` command.
    Head: `free-apple`
    Body:
    1. `run as fake-op %player:name% give @s minecraft:apple`
    """)
@ColorBox(id = 1752895095176L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ See more advanced examples.
    The default config file contains a set of `advanced examples`.
    You can see there are many pre-defined `bundle commands`.
    Especially the `/my-command` examples.

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
    ◉ Define a `/my-tp` command to teleport players to a specified dimension.
    Head: `my-tp resource-world`
    Body:
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
