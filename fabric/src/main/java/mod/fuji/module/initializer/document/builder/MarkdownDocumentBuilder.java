package mod.fuji.module.initializer.document.builder;

import mod.fuji.Fuji;
import mod.fuji.core.auxiliary.IOUtil;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.core.manager.impl.module.ModulePathResolver;
import mod.fuji.module.initializer.document.builder.component.ModuleArgumentTypeAdaptersDocumentBuilder;
import mod.fuji.module.initializer.document.builder.component.ModuleColorBoxesDocumentBuilder;
import mod.fuji.module.initializer.document.builder.component.ModuleCommandsDocumentBuilder;
import mod.fuji.module.initializer.document.builder.component.ModuleConfigurationsDocumentBuilder;
import mod.fuji.module.initializer.document.builder.component.ModuleJobsDocumentBuilder;
import mod.fuji.module.initializer.document.builder.component.ModuleOverviewDocumentBuilder;
import mod.fuji.module.initializer.document.builder.component.ModulePlaceholdersDocumentBuilder;
import mod.fuji.module.initializer.document.builder.component.ReadmeDocumentBuilder;
import mod.fuji.module.initializer.document.builder.context.DocumentBuilderContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

@TestCase(action = "Test the generated document files.", targets = {
    "Check the heading levels, ensure the TOC is generated properly.",
    "Check the `newline` and `indent`.",
    "Check simple files: `afk.effect.md`.",
    "Check the search function: `command_advice.md`",
    "Check `ordered list` and `un-ordered list`: `rank.md`, `rtp.md`",
    "Check the `indent` for a `multi-line list item`: `core.md`",
    "Check complex files: `command_bundle.md`, `command_meta.IF.md`",
    "Check tags escaping: `predicate.md`, `placeholder.md`"
})
public class MarkdownDocumentBuilder {

    public static final Path DOCUMENT_BUILD_DIR = Fuji
        .MOD_CONFIG_PATH
        .resolve("document");

    public static void buildAll() {
        IOUtil.deleteFilesAndPreserveDirs(DOCUMENT_BUILD_DIR.toFile());
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
