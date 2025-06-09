package io.github.sakurawald.core.config.handler.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.sakurawald.Fuji;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
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


public class ResourceConfigurationHandler extends BaseConfigurationHandler<JsonElement> {

    final String resourcePath;

    private ResourceConfigurationHandler(Path path, String resourcePath) {
        super(path);
        this.resourcePath = resourcePath;
    }

    public ResourceConfigurationHandler(@NotNull String resourcePath) {
        this(Fuji.CONFIG_PATH.resolve(resourcePath), resourcePath);
    }

    @SneakyThrows(IOException.class)
    private static @Nullable JsonElement readJsonTreeFromResource(@NotNull String resourcePath) {
        InputStream inputStream = Fuji.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }
        @Cleanup Reader reader = new BufferedReader(new InputStreamReader(inputStream));
        return JsonParser.parseReader(reader);
    }

    private void mergeTree(JsonObject dataTree, JsonObject schemaTree) {
        schemaTree.keySet().stream()
            .filter(key -> !dataTree.has(key))
            .forEach(key -> {
                LogUtil.debug("add missing language key `{}` for file `{}`", key, this.path);
                dataTree.add(key, schemaTree.get(key));
            });
    }

    /**
     * for resource configuration handler, the type of model is JsonElement, which equals to the type of data tree.
     */
    @Override
    protected JsonElement getDefaultModel() {
        return readJsonTreeFromResource(this.resourcePath);
    }

    @Override
    public void readStorage() {
        super.readStorage();

        // add missing language keys
        if (this.model != null) {
            mergeTree(this.model.getAsJsonObject(), this.getDefaultModel().getAsJsonObject());
            this.writeStorage();
        }

    }

}
