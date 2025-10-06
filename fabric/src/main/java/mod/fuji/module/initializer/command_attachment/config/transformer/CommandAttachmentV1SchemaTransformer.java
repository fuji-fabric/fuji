package mod.fuji.module.initializer.command_attachment.config.transformer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mod.fuji.core.auxiliary.ReflectionUtil;
import mod.fuji.core.config.migrator.transformer.abst.JsonConfigurationTransformer;
import mod.fuji.core.config.migrator.transformer.impl.InflateDirectoryIntoSingleFileTransformer;
import mod.fuji.core.config.migrator.version.VersionPropertyInjector;
import mod.fuji.core.service.attachment.AttachmentManager;
import mod.fuji.module.initializer.command_attachment.CommandAttachmentInitializer;
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
        return VersionPropertyInjector.FUTURE_MOD_VERSION;
    }

    @Override
    protected void apply() {
        Path inputDirectoryPath = AttachmentManager.ATTACHMENT_STORAGE_PATH.resolve("command-attachment");
        Path outputFilePath = ReflectionUtil.computeModuleConfigPath(CommandAttachmentInitializer.class).resolve("command-attachment-data.json");
        new InflateDirectoryIntoSingleFileTransformer(inputDirectoryPath, outputFilePath, this::outputArrayProvider, this::mapper)
            .tryApply(getTargetFilePath());
    }
}
