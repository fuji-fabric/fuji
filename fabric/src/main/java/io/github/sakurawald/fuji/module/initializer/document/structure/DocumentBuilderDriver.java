package io.github.sakurawald.fuji.module.initializer.document.structure;

import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.manager.impl.module.ModulePathResolver;
import io.github.sakurawald.fuji.module.initializer.document.builder.DocumentBuilderContext;
import io.github.sakurawald.fuji.module.initializer.document.builder.ModuleDocumentHeaderBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

public class DocumentBuilderDriver {
    private static final Path DOCUMENT_GENERATION_DESTINATION_DIR = Fuji
        .MOD_CONFIG_PATH
        .resolve("document");

    public static void buildAll() {
        ModulePathResolver
            .DECLARED_MODULE_PATH_STRINGS
            .forEach(DocumentBuilderDriver::build);
    }

    @SneakyThrows(IOException.class)
    private static void build(@NotNull String modulePathString) {
//        List<String> modulePathList = ModulePathResolver.toModulePathList(modulePathString);

        /* Generate the document content. */
        StringBuilder documentBuilder = new StringBuilder();
        new ModuleDocumentHeaderBuilder().build(new DocumentBuilderContext(modulePathString, documentBuilder));

        /* Write the document file. */
        String moduleDocumentFileName = getModuleDocumentFileName(modulePathString);
        Path documentFilePath = DOCUMENT_GENERATION_DESTINATION_DIR.resolve(moduleDocumentFileName);
        Files.createDirectories(documentFilePath.getParent());
        Files.writeString(documentFilePath, documentBuilder.toString());
    }

    private static String getModuleDocumentFileName(@NotNull String modulePathString) {
        return modulePathString + ".md";
    }

}
