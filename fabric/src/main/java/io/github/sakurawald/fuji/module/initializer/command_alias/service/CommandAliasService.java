package io.github.sakurawald.fuji.module.initializer.command_alias.service;

import com.mojang.brigadier.CommandDispatcher;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.argument.structure.CommandArgument;
import io.github.sakurawald.fuji.core.command.descriptor.CommandDescriptor;
import io.github.sakurawald.fuji.core.command.processor.CommandAnnotationProcessor;
import io.github.sakurawald.fuji.core.command.structure.CommandRequirementDescriptor;
import io.github.sakurawald.fuji.module.initializer.command_alias.CommandAliasInitializer;
import io.github.sakurawald.fuji.module.initializer.command_alias.structure.AliasCommandDescriptor;
import io.github.sakurawald.fuji.module.initializer.command_alias.structure.CommandAliasEntry;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

public class CommandAliasService {
    public static void registerAllAliasCommands() {
        CommandAliasInitializer.config.model()
            .getAliasCommands()
            .stream()
            .filter(CommandAliasEntry::isEnable)
            .map(CommandAliasService::makeRedirectCommandDescriptor)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(it -> {
                LogUtil.info("Register alias command: {}", it.getCommandSyntax());
                it.register();
            });
        CommandHelper.updateCommandTree();
    }

    public static void unregisterAllAliasCommands() {
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
