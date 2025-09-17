package io.github.sakurawald.fuji.module.initializer.document.builder;

import io.github.sakurawald.fuji.core.document.auxiliary.DocumentUtil;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class ModuleHeaderDocumentBuilder extends DocumentBuilder {

    @Override
    public void build(@NotNull DocumentBuilderContext documentBuilderContext) {
        /* Append the title. */
        documentBuilderContext.getDocumentBuilder()
            .append("# Module: %s".formatted(documentBuilderContext.getModulePathString()))
            .append(System.lineSeparator()).append(System.lineSeparator());

        /* Append the module class document. */
        Optional
            .ofNullable(ModuleManager.MODULE_INITIALIZER_CLASS_BY_MODULE_PATH_STRING.get(documentBuilderContext.getModulePathString()))
            .flatMap(moduleInitializerClass -> DocumentUtil
                .getClassDocumentString(null, moduleInitializerClass)).ifPresent(moduleClassDocument -> {
                documentBuilderContext.getDocumentBuilder()
                    .append("## Module Intro")
                    .append(System.lineSeparator()).append(System.lineSeparator())
                    .append(moduleClassDocument);
            });

    }
}
