package mod.fuji.module.initializer.document.builder.component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.mapper.GsonMapper;
import mod.fuji.core.document.auxiliary.DocumentUtil;
import mod.fuji.module.initializer.document.builder.context.DocumentBuilderContext;
import mod.fuji.module.initializer.document.formatter.MarkdownDocumentFormatter;
import mod.fuji.module.initializer.document.config.adapter.DocumentedTypeAdapterFactory;
import java.util.List;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class ModuleConfigurationsDocumentBuilder extends DocumentBuilder {

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
                    <Admonition type="warning" icon="" title="">
                    **The following JSON content is provided for reference only.**
                    It must NOT be copied directly into the configuration directory, as it does not represent valid JSON syntax.
                    </Admonition>
                    """);

            moduleConfigurationHandlers
                .forEach(it -> build(documentBuilderContext, it));
        }
    }

    public void build(@NotNull DocumentBuilderContext documentBuilderContext, @NotNull BaseConfigurationHandler<?> baseConfigurationHandler) {
        String configFileName = baseConfigurationHandler.getFilePath().getFileName().toString();
        String configFilePath = baseConfigurationHandler.computeRelativePathBasedOnGameDir();

        documentBuilderContext
            .getDocumentBuilder()
            .append(":::config").append(System.lineSeparator())
            .append("- File Name: `%s`".formatted(configFileName)).append(System.lineSeparator());

        Class<?> configModelClass = baseConfigurationHandler.model().getClass();
        DocumentUtil
            .getClassDocumentString(null, configModelClass)
            .ifPresent(configModelClassDocumentString -> {
                configModelClassDocumentString = MarkdownDocumentFormatter.parseDocumentString(configModelClassDocumentString);

                documentBuilderContext
                    .getDocumentBuilder()
                    .append("- Document: %s".formatted(configModelClassDocumentString));
            });

        String jsonString = getDocumentGson().toJson(baseConfigurationHandler.getDefaultModel());

        documentBuilderContext
            .getDocumentBuilder()
            .append("- File Content: ").append(System.lineSeparator())
            .append("""
                <details>

                <summary>_Click to see the `default` content..._</summary>
                """).append(System.lineSeparator())
            .append("```json showLineNumbers title=\"%s\"".formatted(configFilePath)).append(System.lineSeparator())
            .append("%s".formatted(jsonString)).append(System.lineSeparator())
            .append("```").append(System.lineSeparator())
            .append("</details>").append(System.lineSeparator());

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
