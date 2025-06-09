package io.github.sakurawald.module.initializer.command_bundle;

import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.processor.CommandAnnotationProcessor;
import io.github.sakurawald.core.command.structure.CommandDescriptor;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.event.impl.CommandEvents;
import io.github.sakurawald.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.core.gui.CommandDescriptorGui;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.command_bundle.config.model.CommandBundleConfigModel;
import io.github.sakurawald.module.initializer.command_bundle.structure.BundleCommandDescriptor;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.stream.Stream;


@CommandNode("command-bundle")
@CommandRequirement(level = 4)
public class CommandBundleInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CommandBundleConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, CommandBundleConfigModel.class);

    @CommandNode("register")
    @Document("Register all commands defined in bundle-command configuration file.")
    private static int registerAllBundleCommands() {
        LogUtil.info("register bundle commands.");

        config.model().getEntries().stream()
            .map(BundleCommandDescriptor::make)
            .forEach(CommandDescriptor::register);
        CommandHelper.updateCommandTree();
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("un-register")
    @Document("Un-register all bundle-commands registered in server.")
    private static int unregisterAllBundleCommands() {
        LogUtil.info("un-register bundle commands.");

        CommandAnnotationProcessor.descriptors
            .stream()
            .filter(it -> it instanceof BundleCommandDescriptor)
            .forEach(CommandDescriptor::unregister);
        CommandHelper.updateCommandTree();
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("list")
    @Document("List all registered bundle-commands in server.")
    private static int list(@CommandSource CommandContext<ServerCommandSource> ctx) {
        Stream<CommandDescriptor> commandDescriptorStream = CommandAnnotationProcessor.descriptors
            .stream()
            .filter(it -> it instanceof BundleCommandDescriptor);

        if (ctx.getSource().isExecutedByPlayer()) {
            new CommandDescriptorGui(ctx.getSource().getPlayer(), commandDescriptorStream.toList(), 0).open();
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
