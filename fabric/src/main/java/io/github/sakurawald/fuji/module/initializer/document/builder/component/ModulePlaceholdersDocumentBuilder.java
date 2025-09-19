package io.github.sakurawald.fuji.module.initializer.document.builder.component;

import io.github.sakurawald.fuji.core.document.descriptor.PlaceholderDescriptor;
import io.github.sakurawald.fuji.core.document.descriptor.StringDescriptor;
import io.github.sakurawald.fuji.module.initializer.document.builder.context.DocumentBuilderContext;
import io.github.sakurawald.fuji.module.initializer.document.compiler.DocumentCompiler;
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
        String placeholderDocumentString = placeholder.getDocumentString(null).trim();
        placeholderDocumentString = DocumentCompiler.compile(placeholderDocumentString);

        documentBuilderContext
            .getDocumentBuilder()
            .append(":::placeholder").append(System.lineSeparator())
            .append("- Placeholder Name: `%s`".formatted(placeholder.toNameString())).append(System.lineSeparator())
            .append("- Document: %s".formatted(placeholderDocumentString)).append(System.lineSeparator())
            .append(":::").append(System.lineSeparator());
    }

}
