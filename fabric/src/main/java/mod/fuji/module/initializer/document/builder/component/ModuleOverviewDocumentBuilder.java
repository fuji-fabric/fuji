package mod.fuji.module.initializer.document.builder.component;

import mod.fuji.core.document.auxiliary.DocumentUtil;
import mod.fuji.core.manager.impl.module.ModuleManager;
import mod.fuji.module.initializer.document.builder.context.DocumentBuilderContext;
import mod.fuji.module.initializer.document.formatter.MarkdownDocumentFormatter;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class ModuleOverviewDocumentBuilder extends DocumentBuilder {

    @Override
    public void build(@NotNull DocumentBuilderContext documentBuilderContext) {
        /* Append the title. */
        String modulePathString = documentBuilderContext.getModulePathString();
        documentBuilderContext.getDocumentBuilder()
            .append("""
                ---
                title: %s
                ---
                """.formatted(modulePathString)).append(System.lineSeparator()).append(System.lineSeparator())
            .append("# Module: %s".formatted(modulePathString)).append(System.lineSeparator()).append(System.lineSeparator());

        /* Append the module class document. */
        Optional
            .ofNullable(ModuleManager.MODULE_INITIALIZER_CLASS_BY_MODULE_PATH_STRING.get(modulePathString))
            .flatMap(moduleInitializerClass -> DocumentUtil
                .getClassDocumentString(null, moduleInitializerClass)).ifPresent(moduleClassDocument -> {

                String moduleDocumentString = MarkdownDocumentFormatter.parseDocumentString(moduleClassDocument);
                documentBuilderContext.getDocumentBuilder()
                    .append("## Overview").append(System.lineSeparator())
                    .append(":::module").append(System.lineSeparator())
                    .append(moduleDocumentString).append(System.lineSeparator())
                    .append(":::").append(System.lineSeparator());
            });

    }
}
