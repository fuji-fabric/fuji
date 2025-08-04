package io.github.sakurawald.fuji.core.config.transformer.abst;

import com.google.gson.JsonObject;
import com.jayway.jsonpath.DocumentContext;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import java.nio.file.Path;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import org.jetbrains.annotations.NotNull;

public abstract class JsonConfigurationTransformer extends ConfigurationTransformer {

    @Getter(lazy = true)
    private final DocumentContext jsonDocumentContext = makeJsonDocumentContext();

    @SneakyThrows(IOException.class)
    private DocumentContext makeJsonDocumentContext() {
        return BaseConfigurationHandler.getJsonPathParser().parse(this.targetFilePath.toFile());
    }

    public boolean existsJsonPath(@NotNull DocumentContext context, @NotNull String jsonPath) {
        try {
            context.read(jsonPath);
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    public boolean notExistsJsonPath(@NotNull DocumentContext context, @NotNull String jsonPath) {
        return !existsJsonPath(context, jsonPath);
    }

    public Object getJsonPath(@NotNull DocumentContext context, @NotNull String jsonPath) {
        return context.read(jsonPath);
    }

    public void setJsonPath(@NotNull DocumentContext context, @NotNull String jsonPath, @NotNull Object newValue) {
        this.logOperation("Set json path: path = {}, value = {}", jsonPath, newValue);
        context.set(jsonPath, newValue);
    }

    @SneakyThrows(IOException.class)
    public void writeJsonDocumentContextToOriginalFile(@NotNull DocumentContext context) {
        this.logOperation("Write storage.");
        writeJsonObject(context.json(), this.targetFilePath);
    }

    @SneakyThrows
    public void writeJsonObject(@NotNull JsonObject jsonObject, @NotNull Path outputFilePath){
        String json = BaseConfigurationHandler.getGson().toJson(jsonObject);
        Files.writeString(outputFilePath, json);
    }
}
