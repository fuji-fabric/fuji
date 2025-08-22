package io.github.sakurawald.fuji.core.config.handler.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.config.exception.FailedToLoadResourceException;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
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

@ForDeveloper("""
    For resource configuration handler, the type of model is JsonElement, which equals to the type of data tree.
    """)
public abstract class ResourceConfigurationHandler extends BaseConfigurationHandler<JsonElement> {

    protected final @NotNull String resourceClassPath;

    protected ResourceConfigurationHandler(@NotNull Path filePath, @NotNull String resourceClassPath) {
        super(filePath);
        this.resourceClassPath = resourceClassPath;
    }

    @SneakyThrows(IOException.class)
    protected static @Nullable JsonElement readJsonTreeFromResource(@NotNull String resourceClassPath) {
        InputStream inputStream = Fuji.class.getResourceAsStream(resourceClassPath);
        if (inputStream == null) {
            throw new FailedToLoadResourceException("Failed to load specified resource file from class path: %s".formatted(resourceClassPath));
        }
        @Cleanup Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        return JsonParser.parseReader(reader);
    }

    private void mergeTrees(@NotNull JsonObject dataTree, @NotNull JsonObject schemaTree) {
        schemaTree
            .keySet()
            .stream()
            .filter(key -> !dataTree.has(key))
            .forEach(key -> {
                LogUtil.debug("Add missing configuration key `{}` to file `{}`", key, this.filePath);
                JsonElement value = schemaTree.get(key);
                dataTree.add(key, value);
            });
    }

    @Override
    protected void validateModel(@NotNull JsonObject dataTree, @NotNull JsonObject schemaTree) {
        mergeTrees(dataTree, schemaTree);
    }

}
