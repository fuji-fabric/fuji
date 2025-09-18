package io.github.sakurawald.fuji.module.initializer.document.structure;

import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.manager.impl.module.ModulePathResolver;
import io.github.sakurawald.fuji.module.initializer.document.builder.DocumentBuilderContext;
import io.github.sakurawald.fuji.module.initializer.document.builder.ModuleArgumentTypeAdaptersDocumentBuilder;
import io.github.sakurawald.fuji.module.initializer.document.builder.ModuleColorBoxesDocumentBuilder;
import io.github.sakurawald.fuji.module.initializer.document.builder.ModuleCommandsDocumentBuilder;
import io.github.sakurawald.fuji.module.initializer.document.builder.ModuleConfigurationsDocumentBuilder;
import io.github.sakurawald.fuji.module.initializer.document.builder.ModuleOverviewDocumentBuilder;
import io.github.sakurawald.fuji.module.initializer.document.builder.ModuleJobsDocumentBuilder;
import io.github.sakurawald.fuji.module.initializer.document.builder.ModulePlaceholdersDocumentBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

public class DocumentBuilderDriver {
    public static final Path DOCUMENT_BUILD_DIR = Fuji
        .MOD_CONFIG_PATH
        .resolve("document");

    public static void buildAll() {
        /* Build the document for `core` module. */
        build(ModulePathResolver.CORE_MODULE_PATH_STRING);

        /* Build the document for non-`core` module. */
        ModulePathResolver
            .DECLARED_MODULE_PATH_STRINGS
            .forEach(DocumentBuilderDriver::build);
    }

    @SneakyThrows(IOException.class)
    private static void build(@NotNull String modulePathString) {
        /* Build the document. */
        StringBuilder documentBuilder = new StringBuilder();
        DocumentBuilderContext documentBuilderContext = new DocumentBuilderContext(modulePathString, documentBuilder);
        new ModuleOverviewDocumentBuilder().build(documentBuilderContext);
        new ModuleColorBoxesDocumentBuilder().build(documentBuilderContext);
        new ModuleConfigurationsDocumentBuilder().build(documentBuilderContext);
        new ModuleJobsDocumentBuilder().build(documentBuilderContext);
        new ModuleCommandsDocumentBuilder().build(documentBuilderContext);
        new ModulePlaceholdersDocumentBuilder().build(documentBuilderContext);
        new ModuleArgumentTypeAdaptersDocumentBuilder().build(documentBuilderContext);

        /* Make the document file string. */
        String documentFileString = documentBuilder.toString();

        /* Write the document file. */
        String moduleDocumentFileName = getModuleDocumentFileName(modulePathString);
        Path documentFilePath = DOCUMENT_BUILD_DIR.resolve(moduleDocumentFileName);
        Files.createDirectories(documentFilePath.getParent());
        Files.writeString(documentFilePath, documentFileString);
    }

    private static String getModuleDocumentFileName(@NotNull String modulePathString) {
        return modulePathString + ".md";
    }

}
