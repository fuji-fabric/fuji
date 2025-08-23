package io.github.sakurawald.fuji.module.initializer.command_attachment.config.transformer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.config.migrator.transformer.abst.JsonConfigurationTransformer;
import io.github.sakurawald.fuji.core.config.migrator.transformer.impl.InflateDirectoryIntoSingleFileTransformer;
import io.github.sakurawald.fuji.core.config.migrator.version.VersionPropertyInjector;
import io.github.sakurawald.fuji.core.manager.impl.attachment.AttachmentManager;
import io.github.sakurawald.fuji.module.initializer.command_attachment.CommandAttachmentInitializer;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public class CommandAttachmentV1SchemaTransformer extends JsonConfigurationTransformer {

    protected @NotNull JsonArray outputArrayProvider(@NotNull JsonObject root) {
        JsonArray jsonArray = new JsonArray();
        root.add("nodes", jsonArray);
        return jsonArray;
    }

    protected @NotNull JsonObject mapper(@NotNull String inputFileName, @NotNull JsonObject inputJson) {
        JsonObject outputJson = new JsonObject();
        outputJson.addProperty("id", inputFileName);
        outputJson.add("model", inputJson);
        return outputJson;
    }

    @Override
    public String sinceVersion() {
        return VersionPropertyInjector.UNKNOWN_MOD_VERSION;
    }

    @Override
    protected void apply() {
        Path inputDirectoryPath = AttachmentManager.ATTACHMENT_STORAGE_PATH.resolve("command-attachment");
        Path outputFilePath = ReflectionUtil.computeModuleConfigPath(CommandAttachmentInitializer.class).resolve("command-attachment-data.json");
        new InflateDirectoryIntoSingleFileTransformer(inputDirectoryPath, outputFilePath, this::outputArrayProvider, this::mapper)
            .tryApply(getTargetFilePath());
    }
}
