package io.github.sakurawald.fuji.module.initializer.command_attachment.config.transformer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.config.transformer.impl.InflateDirectoryIntoSingleFileTransformer;
import io.github.sakurawald.fuji.core.manager.impl.attachment.AttachmentManager;
import io.github.sakurawald.fuji.module.initializer.command_attachment.CommandAttachmentInitializer;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public class CommandAttachmentV1SchemaTransformer extends InflateDirectoryIntoSingleFileTransformer {

    @Override
    protected @NotNull Path inputDirectoryPath() {
        return AttachmentManager.ATTACHMENT_STORAGE_PATH.resolve("command-attachment");
    }

    @Override
    protected @NotNull Path outputFilePath() {
        return ReflectionUtil.computeModuleConfigPath(CommandAttachmentInitializer.class).resolve("command-attachment-data.json");
    }

    @Override
    protected @NotNull JsonArray arrayMaker(@NotNull JsonObject root) {
        JsonArray jsonArray = new JsonArray();
        root.add("nodes", jsonArray);
        return jsonArray;
    }

    @Override
    protected @NotNull JsonObject mapper(@NotNull String fileName, @NotNull JsonObject inputJson) {
        JsonObject outputJson = new JsonObject();
        outputJson.addProperty("id", fileName);
        outputJson.add("model", inputJson);
        return outputJson;
    }
}
