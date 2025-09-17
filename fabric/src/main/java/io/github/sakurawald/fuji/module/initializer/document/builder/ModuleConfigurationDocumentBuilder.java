package io.github.sakurawald.fuji.module.initializer.document.builder;

import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.document.auxiliary.DocumentUtil;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ModuleConfigurationDocumentBuilder extends DocumentBuilder{

    @Override
    public void build(@NotNull DocumentBuilderContext documentBuilderContext) {
        List<BaseConfigurationHandler<?>> moduleConfigurationHandlers = DocumentUtil
            .getObjectConfigurationHandlers()
            .stream()
            .filter(it -> it.getSourceModule().equals(documentBuilderContext.getModulePathString()))
            .toList();

        if (!moduleConfigurationHandlers.isEmpty()) {
            documentBuilderContext
                .getDocumentBuilder()
                .append("## Configuration")
                .append(System.lineSeparator());

            moduleConfigurationHandlers
                .forEach(it -> build(documentBuilderContext, it));
        }
    }

    public void build(@NotNull DocumentBuilderContext documentBuilderContext, @NotNull BaseConfigurationHandler<?> baseConfigurationHandler) {

        documentBuilderContext
            .getDocumentBuilder()
            .append("### Config File: %s".formatted(baseConfigurationHandler.getFilePath().getFileName()))
            .append(System.lineSeparator())
            .append("File Path: `%s`".formatted(baseConfigurationHandler.computeRelativePathBasedOnGameDir()))
            .append(System.lineSeparator());

        Class<?> configModelClass = baseConfigurationHandler.model().getClass();
        DocumentUtil
            .getClassDocumentString(null, configModelClass)
            .ifPresent(configModelClassDocumentString -> {
                documentBuilderContext
                    .getDocumentBuilder()
                    .append("Document: %s".formatted(configModelClassDocumentString));
            });

    }

}
