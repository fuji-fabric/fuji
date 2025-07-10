package io.github.sakurawald.fuji.module.initializer.command_bundle;

import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.processor.CommandAnnotationProcessor;
import io.github.sakurawald.fuji.core.command.structure.CommandDescriptor;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.event.impl.CommandEvents;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.document.gui.CommandsInspectionGui;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_bundle.config.model.CommandBundleConfigModel;
import io.github.sakurawald.fuji.module.initializer.command_bundle.structure.BundleCommandDescriptor;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.stream.Stream;

@Document(id = 1751826356909L, value = """
    This module allows you to create new command:
    1. The new command can accept arguments.
    2. The body of the new command, can be a list of commands.
    """)
@ColorBox(id = 1751870454656L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    ◉ The features of this module:
    1. Provide a user-friendly DSL, to create `new custom commands` easily.
    2. Support the inter-operation with `user-defined variable`, `placeholders` and `target selectors`.
    3. Support complex `argument types`: `required argument`, `literal argument` and even `optional argument with a specified default value`.
    4. Pre-defined `type-system`, to use the built-in `argument type`.
    5. Register and un-register commands on the fly, without a server re-start.
    """)
@ColorBox(id = 1751870456781L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    ◉ To query all supported `argument types`:
    Issue `/fuji inspect argument-types`.
    """)
@ColorBox(id = 1751870458514L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    You can use command generator to get powerful commands:
    https://www.gamergeeks.net/apps/minecraft/particle-command-generator
    """)
@ColorBox(id = 1751901598337L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Combine many commands into one command.
    Pattern: `composite-heal`
    Bundle:
    1. `say before heal %player:name%`
    2. `run as fake-op %player:name% particle minecraft:heart ~ ~2 ~`
    3. `run as player %player:name% heal`
    4. `say after heal %player:name%`
    """)
@ColorBox(id = 1751901750629L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Transform the form of a command. (Can be used as a shortcut command)
    Pattern: `warn \\<player player-arg\\> \\<greedy greedy-arg\\>`
    Bundle:
    1. `run as player %player:name% send-message $player-arg \\<red\\>You are warned: $greedy-arg`
    """)
@ColorBox(id = 1751983696805L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Transform the form of a command. (List 1)
    - `/blocknbt` -> `/data get block`
    - `/entitynbt` -> `/data get entity`
    - `/gm`, `/gms` and `/gmc` -> `/gamemode`
    - `/findbiome` -> `/locate biome`
    - `/flyspeed` -> `/attribute \\<player\\> minecraft:generic.flying_speed`
    - `/walkspeed` -> `/attribute \\<player\\> minecraft:movement_speed`
    - `/maxhealth` -> `/attribute \\<player\\> minecraft:generic.max_health`
    - `/groundclean` -> `/kill @e[type=...]`
    - `/spawnmob` -> `/summon`
    - `/shoot` -> `/summon` with motion
    - `/smite` and `/thunder` -> `/summon minecraft:lighting_bolt`
    """)


@CommandNode("command-bundle")
@CommandRequirement(level = 4)
public class CommandBundleInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CommandBundleConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, CommandBundleConfigModel.class);

    @Document(id = 1751826359683L, value = "Register all commands defined in bundle-command configuration file.")
    @CommandNode("register")
    private static int $registerAllBundleCommands() {
        LogUtil.info("Register bundle commands.");

        config.model().getEntries().stream()
            .map(BundleCommandDescriptor::make)
            .forEach(it -> {
                LogUtil.info("Register bundle command: {}", it.getCommandSyntax());
                it.register();
            });
        CommandHelper.updateCommandTree();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826362252L, value = "Un-register all bundle-commands registered in server.")
    @CommandNode("un-register")
    private static int $unregisterAllBundleCommands() {
        LogUtil.info("Un-register bundle commands.");

        CommandAnnotationProcessor.REGISTERED_COMMAND_DESCRIPTORS
            .stream()
            .filter(it -> it instanceof BundleCommandDescriptor)
            .forEach(it -> {
                LogUtil.info("Un-register bundle command: {}", it.getCommandSyntax());
                it.unregister();
            });
        CommandHelper.updateCommandTree();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826364625L, value = "List all registered bundle-commands in server.")
    @CommandNode("list")
    private static int $list(@CommandSource CommandContext<ServerCommandSource> ctx) {
        Stream<CommandDescriptor> commandDescriptorStream = CommandAnnotationProcessor.REGISTERED_COMMAND_DESCRIPTORS
            .stream()
            .filter(it -> it instanceof BundleCommandDescriptor);

        if (ctx.getSource().isExecutedByPlayer()) {
            new CommandsInspectionGui(null, ctx.getSource().getPlayer(), commandDescriptorStream.toList(), 0).open();
        } else {
            commandDescriptorStream.forEach(it -> ctx.getSource().sendMessage(Text.literal(it.getCommandNodePath())));
        }

        return CommandHelper.Return.SUCCESS;
    }

    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // register in server started.
            $registerAllBundleCommands();

            // to register bundle-commands automatically after `/reload` command.
            CommandEvents.REGISTRATION.register((a, b, c) -> $registerAllBundleCommands());
        });
    }

    @Override
    protected void onReload() {
        $unregisterAllBundleCommands();
        $registerAllBundleCommands();
    }

}
