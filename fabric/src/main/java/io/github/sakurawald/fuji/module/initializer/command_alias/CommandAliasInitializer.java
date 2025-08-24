package io.github.sakurawald.fuji.module.initializer.command_alias;

import com.mojang.brigadier.CommandDispatcher;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.argument.structure.CommandArgument;
import io.github.sakurawald.fuji.core.command.descriptor.CommandDescriptor;
import io.github.sakurawald.fuji.core.command.processor.CommandAnnotationProcessor;
import io.github.sakurawald.fuji.core.command.structure.CommandRequirementDescriptor;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.event.impl.CommandEvents;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_alias.config.model.CommandAliasConfigModel;
import io.github.sakurawald.fuji.module.initializer.command_alias.structure.AliasCommandDescriptor;
import io.github.sakurawald.fuji.module.initializer.command_alias.structure.CommandAliasEntry;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

@Document(id = 1751826302190L, value = """
    This module allows you to define alias for existing commands.
    The defined alies command will redirect to the existing command node.
    """)
@ColorBox(id = 1751900420030L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Define a `new shortcut command` as an alias to `an existing command`.
    For example:
    1. Define a new `/r` command, as alias to `/reply` command.
    2. Define a new `/sudo` command, as alias to `/run as fake-op` command.
    """)
@ColorBox(id = 1751900579678L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    The `command_alias` command only has the ability to define `a new command`, as the alias of `an existing command`.
    It works by `command redirecting`.
    You can't use `command_alias` module to actually define `a new command with arguments`, that's the weakness.
    If you want to define `a new command with arguments`, see `command_bundle` module.

    In short:
    1. To define `a simple command` with no arguments, use `command_alias` module.
    2. To define `a complex command` with arguments, use `command_bundle` module.
    """)
public class CommandAliasInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<CommandAliasConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, CommandAliasConfigModel.class);

    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            registerAllAliasCommands();
            CommandEvents.REGISTRATION.register((a, b, c) -> registerAllAliasCommands());
        });
    }

    @Override
    protected void onReload() {
        unregisterAllAliasCommands();
        registerAllAliasCommands();
    }

    private static void registerAllAliasCommands() {
        config.model()
            .getAliases()
            .stream()
            .map(CommandAliasInitializer::makeRedirectCommandDescriptor)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(it -> {
                LogUtil.info("Register alias command: {}", it.getCommandSyntax());
                it.register();
            });
        CommandHelper.updateCommandTree();
    }

    private static void unregisterAllAliasCommands() {
        List<CommandDescriptor> registeredCommandDescriptors = getRegisteredAliasCommandDescriptors();

        LogUtil.info("Un-register alias commands.");
        registeredCommandDescriptors
            .forEach(it -> {
                LogUtil.info("Un-register alias command: {}", it.getCommandSyntax());
                it.unregister();
            });
        CommandHelper.updateCommandTree();
    }

    private static List<CommandDescriptor> getRegisteredAliasCommandDescriptors() {
        return CommandAnnotationProcessor.REGISTERED_COMMAND_DESCRIPTORS
            .stream()
            .filter(it -> it instanceof AliasCommandDescriptor)
            .toList();
    }

    private static Optional<AliasCommandDescriptor> makeRedirectCommandDescriptor(@NotNull CommandAliasEntry entry) {
        /* Find the redirect target command node in server command tree. */
        CommandDispatcher<ServerCommandSource> dispatcher = CommandHelper.getCommandDispatcher();
        return Optional.ofNullable(dispatcher.findNode(entry.getTo()))
            .map(redirectTargetCommandNode -> {
                CommandRequirementDescriptor requirement = entry.getRequirement();
                List<CommandArgument> commandArguments = entry.getFrom()
                    .stream()
                    .map(argumentName -> CommandArgument.ofLiteralArgument(argumentName, requirement))
                    .toList();

                AliasCommandDescriptor descriptor = new AliasCommandDescriptor(commandArguments, redirectTargetCommandNode);
                descriptor.fillDocument(entry.getDocument());

                return Optional.of(descriptor);
            })
            .orElseGet(() -> {
                LogUtil.warn("Failed to find the target command node {} in server command tree. Ignoring the command alias entry: {}", entry.getTo(), entry);
                return Optional.empty();
            });
    }

}
