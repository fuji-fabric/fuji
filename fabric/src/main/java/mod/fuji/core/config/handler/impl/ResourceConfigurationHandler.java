package mod.fuji.core.config.handler.impl;

import com.google.gson.JsonObject;
import mod.fuji.Fuji;
import mod.fuji.core.config.exception.FailedToLoadResourceException;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.mapper.GsonMapper;
import mod.fuji.core.config.validator.MissingJsonKeysValidator;
import java.nio.charset.StandardCharsets;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;

/**
 *     For resource configuration handler, the type of model is JsonElement, which equals to the type of data tree.

 **/
public abstract class ResourceConfigurationHandler extends BaseConfigurationHandler<JsonObject> {

    protected final @NotNull String resourceClassPath;

    protected ResourceConfigurationHandler(@NotNull Path filePath, @NotNull String resourceClassPath) {
        super(filePath);
        this.resourceClassPath = resourceClassPath;
    }

    @SneakyThrows(IOException.class)
    protected static @Nullable JsonObject loadJsonFileFromResourceClassPath(@NotNull String resourceClassPath) {
        InputStream inputStream = Fuji.class.getResourceAsStream(resourceClassPath);
        if (inputStream == null) {
            throw new FailedToLoadResourceException("Failed to load specified resource file from class path: %s".formatted(resourceClassPath));
        }
        @Cleanup Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        return GsonMapper.fromJson(reader, JsonObject.class);
    }

    @Override
    protected void validateModel(@NotNull JsonObject dataTree, @NotNull JsonObject schemaTree) {
        MissingJsonKeysValidator.mergeTrees(this, dataTree, schemaTree);
    }

}
