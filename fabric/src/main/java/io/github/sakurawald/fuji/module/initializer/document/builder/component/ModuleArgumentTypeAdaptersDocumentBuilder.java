package io.github.sakurawald.fuji.module.initializer.document.builder.component;

import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.module.initializer.document.builder.context.DocumentBuilderContext;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ModuleArgumentTypeAdaptersDocumentBuilder extends DocumentBuilder{

    @Override
    public void build(@NotNull DocumentBuilderContext documentBuilderContext) {
        List<BaseArgumentTypeAdapter> elements = BaseArgumentTypeAdapter.Registry.REGISTERED_COMMAND_ARGUMENT_TYPE_ADAPTERS
            .stream()
            .filter(it -> it.getSourceModule().equals(documentBuilderContext.getModulePathString()))
            .toList();

        if (!elements.isEmpty()) {
            documentBuilderContext
                .getDocumentBuilder()
                .append("## Argument Types").append(System.lineSeparator());

            elements.forEach(it -> build(documentBuilderContext, it));
        }
    }

    private void build(@NotNull DocumentBuilderContext documentBuilderContext, BaseArgumentTypeAdapter it) {
        documentBuilderContext
            .getDocumentBuilder()
            .append(":::argument-type").append(System.lineSeparator())
            .append("- Argument Type Name: `%s`".formatted(it.getTypeNames())).append(System.lineSeparator())
            .append("- Argument Type Class: `%s`".formatted(it.getTypeClasses().stream().map(Class::getSimpleName).toList())).append(System.lineSeparator())
            .append(":::").append(System.lineSeparator());
    }
}
