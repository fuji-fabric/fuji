package io.github.sakurawald.fuji.module.initializer.document.builder.component;

import io.github.sakurawald.fuji.core.command.descriptor.CommandDescriptor;
import io.github.sakurawald.fuji.core.command.structure.CommandRequirementDescriptor;
import io.github.sakurawald.fuji.core.document.auxiliary.DocumentUtil;
import io.github.sakurawald.fuji.module.initializer.document.builder.context.DocumentBuilderContext;
import io.github.sakurawald.fuji.module.initializer.document.formatter.MarkdownDocumentFormatter;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModuleCommandsDocumentBuilder extends DocumentBuilder {

    @Override
    public void build(@NotNull DocumentBuilderContext documentBuilderContext) {
        List<CommandDescriptor> commands = DocumentUtil
            .getCommandDescriptors()
            .stream()
            .filter(it -> it.getSourceModule().equals(documentBuilderContext.getModulePathString()))
            .toList();

        if (!commands.isEmpty()) {
            documentBuilderContext
                .getDocumentBuilder()
                .append("## Commands").append(System.lineSeparator());

            commands.forEach(it -> build(documentBuilderContext, it));
        }

    }

    private void build(@NotNull DocumentBuilderContext documentBuilderContext, @NotNull CommandDescriptor commandDescriptor) {
        String commandSyntax = commandDescriptor.getCommandSyntax();
        boolean canBeExecutedByConsole = commandDescriptor.canBeExecutedByConsole();
        CommandRequirementDescriptor commandRequirement = CommandDescriptor.CommandRequirement.computeCommandRequirement(commandDescriptor);

        @Nullable String commandDocumentString = Optional
            .ofNullable(commandDescriptor.document)
            .map(MarkdownDocumentFormatter::parseDocumentString)
            .orElse(null);

        documentBuilderContext
            .getDocumentBuilder()
            .append(":::command").append(System.lineSeparator())
            .append("- Command Syntax: `%s`".formatted(commandSyntax)).append(System.lineSeparator())
            .append("- Document: %s".formatted(commandDocumentString)).append(System.lineSeparator())
            .append("- Can be executed by console: `%s`".formatted(canBeExecutedByConsole)).append(System.lineSeparator())
            .append("- Required Level Permission: `%s`".formatted(commandRequirement.getLevel())).append(System.lineSeparator())
            .append("- Required String Permission: `%s`".formatted(commandRequirement.getString())).append(System.lineSeparator())
            .append(":::").append(System.lineSeparator());
    }

}
