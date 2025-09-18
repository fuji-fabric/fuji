package io.github.sakurawald.fuji.module.initializer.document.builder;

import io.github.sakurawald.fuji.core.document.descriptor.PlaceholderDescriptor;
import io.github.sakurawald.fuji.core.document.descriptor.StringDescriptor;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ModulePlaceholdersDocumentBuilder extends DocumentBuilder{
    @Override
    public void build(@NotNull DocumentBuilderContext documentBuilderContext) {
        List<StringDescriptor> placeholders = PlaceholderDescriptor
            .getPlaceholderDescriptors()
            .stream()
            .filter(it -> it.getSourceModule().equals(documentBuilderContext.getModulePathString()))
            .toList();

        if (!placeholders.isEmpty()) {
            documentBuilderContext
                .getDocumentBuilder()
                .append("## Placeholders").append(System.lineSeparator());

            placeholders.forEach(it -> build(documentBuilderContext, it));
        }
    }

    private void build(@NotNull DocumentBuilderContext documentBuilderContext, StringDescriptor placeholder) {
        String documentString = placeholder.getDocumentString(null).trim();

        documentBuilderContext
            .getDocumentBuilder()
            .append(":::placeholder").append(System.lineSeparator())
            .append("- Placeholder Name: `%s`".formatted(placeholder.toNameString())).append(System.lineSeparator())
            .append("- Document: %s".formatted(documentString)).append(System.lineSeparator())
            .append(":::").append(System.lineSeparator());
    }

}
