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

    protected final String resourcePath;

    private ResourceConfigurationHandler(Path path, String resourcePath) {
        super(path);
        this.resourcePath = resourcePath;
    }

    public ResourceConfigurationHandler(@NotNull String resourcePath) {
        this(Fuji.MOD_CONFIG_PATH.resolve(resourcePath), resourcePath);
    }

    @SneakyThrows(IOException.class)
    protected static @Nullable JsonElement readJsonTreeFromResource(@NotNull String resourcePath) {
        InputStream inputStream = Fuji.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new FailedToLoadResourceException("Failed to load resource from virtual jar stream: " + resourcePath);
        }
        @Cleanup Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        return JsonParser.parseReader(reader);
    }

    private void mergeTree(JsonObject dataTree, JsonObject schemaTree) {
        schemaTree
            .keySet()
            .stream()
            .filter(key -> !dataTree.has(key))
            .forEach(key -> {
                LogUtil.debug("Add missing configuration key `{}` to file `{}`", key, this.path);
                JsonElement value = schemaTree.get(key);
                dataTree.add(key, value);
            });
    }

    @Override
    public void readStorage() {
        super.readStorage();

        /* Add missing configuration keys. */
        if (this.model != null) {
            mergeTree(this.model.getAsJsonObject(), this.getDefaultModel().getAsJsonObject());
            this.writeStorage();
        }
    }
}
