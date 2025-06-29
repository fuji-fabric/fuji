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

@Document("""
    This module allows you to create new command:
    1. The new command can accept arguments.
    2. The body of the new command, can be a list of commands.
    """)

@ColorBox(color = ColorBox.ColorBlockTypes.NOTE, value = """
    The features of this module:
    1. Provide a user-friendly DSL, to create `new custom commands` easily.
    2. Support the inter-operation with `user-defined variable`, `placeholders` and `target selectors`.
    3. Support complex `argument types`: `required argument`, `literal argument` and even `optional argument with a specified default value`.
    4. Pre-defined `type-system`, to use the built-in `argument type`.
    5. Register and un-register commands on the fly, without a server re-start.
    """)

@ColorBox(color = ColorBox.ColorBlockTypes.TIPS, value = """
    To query all supported `argument types`:
    Issue `/fuji inspect argument-types`.
    """)

@ColorBox(color = ColorBox.ColorBlockTypes.TIPS, value = """
    You can use command generator to get powerful commands:
    https://www.gamergeeks.net/apps/minecraft/particle-command-generator
    """)



@CommandNode("command-bundle")
@CommandRequirement(level = 4)
public class CommandBundleInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CommandBundleConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, CommandBundleConfigModel.class);

    @Document("Register all commands defined in bundle-command configuration file.")
    @CommandNode("register")
    private static int registerAllBundleCommands() {
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

    @Document("Un-register all bundle-commands registered in server.")
    @CommandNode("un-register")
    private static int unregisterAllBundleCommands() {
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

    @Document("List all registered bundle-commands in server.")
    @CommandNode("list")
    private static int list(@CommandSource CommandContext<ServerCommandSource> ctx) {
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
            registerAllBundleCommands();

            // to register bundle-commands automatically after `/reload` command.
            CommandEvents.REGISTRATION.register((a, b, c) -> registerAllBundleCommands());
        });
    }

    @Override
    protected void onReload() {
        unregisterAllBundleCommands();
        registerAllBundleCommands();
    }

}
