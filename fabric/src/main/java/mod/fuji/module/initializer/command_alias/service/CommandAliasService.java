package mod.fuji.module.initializer.command_alias.service;

import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.descriptor.CommandDescriptor;
import mod.fuji.core.command.processor.CommandAnnotationProcessor;
import mod.fuji.core.command.structure.CommandRequirementDescriptor;
import mod.fuji.module.initializer.command_alias.CommandAliasInitializer;
import mod.fuji.module.initializer.command_alias.structure.AliasCommandDescriptor;
import mod.fuji.module.initializer.command_alias.structure.CommandAliasEntry;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class CommandAliasService {
    public static void registerAllAliasCommands() {
        getDeclaredAliasCommandDescriptors()
            .stream()
            .map(CommandAliasService::makeRedirectCommandDescriptor)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(CommandDescriptor::register);
        CommandHelper.Tree.updateCommandTree();
    }

    private static List<CommandAliasEntry> getDeclaredAliasCommandDescriptors() {
        return CommandAliasInitializer.config.model()
            .getAliasCommands()
            .stream()
            .filter(CommandAliasEntry::isEnable)
            .toList();
    }

    public static void unregisterAllAliasCommands() {
        List<CommandDescriptor> registeredCommandDescriptors = getRegisteredAliasCommandDescriptors();

        registeredCommandDescriptors
            .forEach(CommandDescriptor::unregister);
        CommandHelper.Tree.updateCommandTree();
    }

    private static List<CommandDescriptor> getRegisteredAliasCommandDescriptors() {
        return CommandAnnotationProcessor.REGISTERED_COMMAND_DESCRIPTORS
            .stream()
            .filter(it -> it instanceof AliasCommandDescriptor)
            .toList();
    }

    private static Optional<AliasCommandDescriptor> makeRedirectCommandDescriptor(@NotNull CommandAliasEntry entry) {
        /* Find the redirect target command node in server command tree. */
        return CommandHelper.Node.findCommandNode(entry.getTo())
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
                LogUtil.warn("Failed to find the target command node {} in server command tree, ignoring the command alias entry: {}", entry.getTo(), entry);
                return Optional.empty();
            });
    }

}
