package io.github.sakurawald.fuji.module.initializer.document.builder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.mapper.GsonMapper;
import io.github.sakurawald.fuji.core.document.auxiliary.DocumentUtil;
import io.github.sakurawald.fuji.module.initializer.document.config.adapter.DocumentedTypeAdapterFactory;
import java.util.List;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class ModuleConfigurationDocumentBuilder extends DocumentBuilder {

    @Getter(lazy = true)
    private static final Gson documentGson = makeDocumentGson();

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
                .append("## Configurations")
                .append(System.lineSeparator());

            documentBuilderContext
                .getDocumentBuilder()
                .append("""
                    :::warning
                    The JSON content is provided for documentation purposes only.

                    It should not be copied directly into your configuration folder, as the document format is not valid JSON syntax.
                    :::
                    """);

            moduleConfigurationHandlers
                .forEach(it -> build(documentBuilderContext, it));
        }
    }

    public void build(@NotNull DocumentBuilderContext documentBuilderContext, @NotNull BaseConfigurationHandler<?> baseConfigurationHandler) {

        documentBuilderContext
            .getDocumentBuilder()
            .append(":::config").append(System.lineSeparator())
            .append("- File Name: `%s`".formatted(baseConfigurationHandler.getFilePath().getFileName())).append(System.lineSeparator())
            .append("- File Path: `%s`".formatted(baseConfigurationHandler.computeRelativePathBasedOnGameDir())).append(System.lineSeparator());

        Class<?> configModelClass = baseConfigurationHandler.model().getClass();
        DocumentUtil
            .getClassDocumentString(null, configModelClass)
            .ifPresent(configModelClassDocumentString -> {
                documentBuilderContext
                    .getDocumentBuilder()
                    .append("- Document: %s".formatted(configModelClassDocumentString));
            });

        String jsonString = getDocumentGson().toJson(baseConfigurationHandler.getDefaultModel());

        documentBuilderContext
            .getDocumentBuilder()
            .append("- File Content: ").append(System.lineSeparator())
            .append("```json showLineNumbers").append(System.lineSeparator())
            .append("%s".formatted(jsonString)).append(System.lineSeparator())
            .append("```").append(System.lineSeparator());

        documentBuilderContext
            .getDocumentBuilder()
            .append(":::").append(System.lineSeparator());

    }

    private static @NotNull Gson makeDocumentGson() {
        GsonBuilder gsonBuilder = GsonMapper
            .__GetInternalGsonReferenceWithoutTheUseOfWrappedFunctions()
            .newBuilder()
            .registerTypeAdapterFactory(new DocumentedTypeAdapterFactory());
        return gsonBuilder.create();
    }

}
