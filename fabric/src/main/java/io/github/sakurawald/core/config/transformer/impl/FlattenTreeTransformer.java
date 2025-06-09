package io.github.sakurawald.core.config.transformer.impl;

import com.google.gson.JsonObject;
import com.jayway.jsonpath.DocumentContext;
import io.github.sakurawald.core.auxiliary.JsonUtil;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.transformer.abst.ConfigurationTransformer;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public class FlattenTreeTransformer extends ConfigurationTransformer {

    private final String subtreeIdentifier;
    private final String jsonPath;
    private final String topLevel;
    private final Function<String, Path> level2outPath;

    private boolean overrideTheOriginalFileWithSkeletonTree;

    public FlattenTreeTransformer(String jsonPath, String subtreeIdentifier, String topLevel, Function<String, Path> level2outPath) {
        this.subtreeIdentifier = subtreeIdentifier;
        this.jsonPath = jsonPath;
        this.topLevel = topLevel;
        this.level2outPath = level2outPath;
    }

    @SneakyThrows(IOException.class)
    private void flatten(JsonObject parent, String level) {
        /* find and walk submodule */
        parent.keySet().stream()
            .filter(key -> parent.get(key).isJsonObject() && parent.getAsJsonObject(key).has(subtreeIdentifier))
            .forEach(key -> {
                String nextLevel = StringUtils.strip(level + "." + key, ".");
                flatten(parent.getAsJsonObject(key), nextLevel);
            });

        /* create top-level file to store the tree */
        parent.remove(subtreeIdentifier);

        // remove empty subtree
        parent.keySet().stream().toList().stream()
            .filter(key -> parent.get(key).isJsonObject() && JsonUtil.isEmpty(parent.getAsJsonObject(key)))
            .forEach(parent::remove);

        // if the tree is not empty, migrate it to a standalone file
        Path currentTreeOutPath = level2outPath.apply(level);
        if (!JsonUtil.isEmpty(parent) && Files.notExists(currentTreeOutPath)) {
            logConsole("flatten tree `{}` into the file `{}`", level, currentTreeOutPath);
            Files.createDirectories(currentTreeOutPath.getParent());
            String json = BaseConfigurationHandler.getGson().toJson(parent);
            Files.writeString(currentTreeOutPath, json);

            this.overrideTheOriginalFileWithSkeletonTree = true;
        }

        /* remove all keys on leave */
        parent.keySet().stream().toList()
            .forEach(parent::remove);
    }

    private JsonObject makeSkeletonTree(JsonObject parent) {
        parent.keySet().stream().toList().stream()
            .filter(key -> !key.equals(subtreeIdentifier))
            .forEach(key -> {
                if (parent.get(key).isJsonObject()) {
                    makeSkeletonTree(parent.getAsJsonObject(key));

                    // remove the subtree if empty. (the subtree will not be empty, if it is a sub-module
                    if (JsonUtil.isEmpty(parent.getAsJsonObject(key))) parent.remove(key);
                } else parent.remove(key);
            });
        return parent;
    }

    @Override
    public void apply() {
        DocumentContext context = this.makeDocumentContext();

        JsonObject root = (JsonObject) read(context, this.jsonPath);
        this.flatten(root, this.topLevel);

        if (overrideTheOriginalFileWithSkeletonTree) {
            JsonObject skeletonTree = (JsonObject) read(context, this.jsonPath);
            set(context, this.jsonPath, makeSkeletonTree(skeletonTree));
            writeStorage(context);
        }
    }

}
