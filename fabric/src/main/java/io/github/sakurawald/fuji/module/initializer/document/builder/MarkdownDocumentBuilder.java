package io.github.sakurawald.fuji.module.initializer.document.builder;

import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.manager.impl.module.ModulePathResolver;
import io.github.sakurawald.fuji.module.initializer.document.builder.component.ModuleArgumentTypeAdaptersDocumentBuilder;
import io.github.sakurawald.fuji.module.initializer.document.builder.component.ModuleColorBoxesDocumentBuilder;
import io.github.sakurawald.fuji.module.initializer.document.builder.component.ModuleCommandsDocumentBuilder;
import io.github.sakurawald.fuji.module.initializer.document.builder.component.ModuleConfigurationsDocumentBuilder;
import io.github.sakurawald.fuji.module.initializer.document.builder.component.ModuleJobsDocumentBuilder;
import io.github.sakurawald.fuji.module.initializer.document.builder.component.ModuleOverviewDocumentBuilder;
import io.github.sakurawald.fuji.module.initializer.document.builder.component.ModulePlaceholdersDocumentBuilder;
import io.github.sakurawald.fuji.module.initializer.document.builder.component.ReadmeDocumentBuilder;
import io.github.sakurawald.fuji.module.initializer.document.builder.context.DocumentBuilderContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

public class MarkdownDocumentBuilder {
    public static final Path DOCUMENT_BUILD_DIR = Fuji
        .MOD_CONFIG_PATH
        .resolve("document");

    public static void buildAll() {
        buildReadme();
        buildModules();
    }

    @SneakyThrows(IOException.class)
    private static void buildReadme() {
        /* Build the readme file. */
        StringBuilder documentBuilder = new StringBuilder();
        DocumentBuilderContext documentBuilderContext = new DocumentBuilderContext(ModulePathResolver.CORE_MODULE_PATH_STRING, documentBuilder);
        new ReadmeDocumentBuilder().build(documentBuilderContext);

        /* Make the readme file string. */
        String readmeFileString = documentBuilder.toString();

        /* Write the readme file. */
        Path readmeFilePath = DOCUMENT_BUILD_DIR.resolve("01-README.md");
        Files.createDirectories(readmeFilePath.getParent());
        Files.writeString(readmeFilePath, readmeFileString);
    }

    private static void buildModules() {
        /* Build the document for `core` module. */
        buildModule(ModulePathResolver.CORE_MODULE_PATH_STRING);

        /* Build the document for non-`core` module. */
        ModulePathResolver
            .DECLARED_MODULE_PATH_STRINGS
            .forEach(MarkdownDocumentBuilder::buildModule);
    }

    @SneakyThrows(IOException.class)
    private static void buildModule(@NotNull String modulePathString) {
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
        Path documentFilePath = DOCUMENT_BUILD_DIR.resolve("02-Modules").resolve(moduleDocumentFileName);
        Files.createDirectories(documentFilePath.getParent());
        Files.writeString(documentFilePath, documentFileString);
    }

    private static String getModuleDocumentFileName(@NotNull String modulePathString) {
        return modulePathString + ".md";
    }

}
