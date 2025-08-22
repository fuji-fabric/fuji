package io.github.sakurawald.fuji.core.config.migrator.transformer.impl;

import com.google.gson.JsonObject;
import com.jayway.jsonpath.DocumentContext;
import io.github.sakurawald.fuji.core.auxiliary.JsonUtil;
import io.github.sakurawald.fuji.core.auxiliary.StringUtil;
import io.github.sakurawald.fuji.core.config.mapper.GsonMapper;
import io.github.sakurawald.fuji.core.config.migrator.transformer.abst.JsonConfigurationTransformer;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

@ForDeveloper("""
    This transformer is used to flatten a tree represented in single file into multiple files.
    """)
@Deprecated
public class FlattenTreeTransformer extends JsonConfigurationTransformer {

    private final String rootTreePath;
    private final String subtreeIdentifier;
    private final String walkingPath;
    private final Function<String, Path> outputFilePathMapper;

    private boolean overrideTheOriginalFileWithSkeletonTree;

    public FlattenTreeTransformer(@NotNull String rootTreePath, @NotNull String subtreeIdentifier, @NotNull String walkingPath, @NotNull Function<String, Path> outputFilePathMapper) {
        this.rootTreePath = rootTreePath;
        this.subtreeIdentifier = subtreeIdentifier;
        this.walkingPath = walkingPath;
        this.outputFilePathMapper = outputFilePathMapper;
    }

    @SneakyThrows(IOException.class)
    private void flatten(@NotNull JsonObject parentTree, @NotNull String walkingPath) {
        /* Filter all subtree using the subtree identifier. */
        parentTree.keySet()
            .stream()
            .filter(key -> parentTree.get(key).isJsonObject() && parentTree.getAsJsonObject(key).has(subtreeIdentifier))
            .forEach(key -> {
                String newWalkingPath = StringUtil.trimPathString(walkingPath + "." + key);
                flatten(parentTree.getAsJsonObject(key), newWalkingPath);
            });

        /* Remove the subtree from the original json tree. */
        parentTree.remove(subtreeIdentifier);

        /* Remove the empty-tree from the original json tree. */
        parentTree.keySet()
            .stream()
            .toList()
            .stream()
            .filter(key -> parentTree.get(key).isJsonObject() && JsonUtil.isEmpty(parentTree.getAsJsonObject(key)))
            .forEach(parentTree::remove);

        /* If the specified subtree is not empty, migrate it into a standalone file. */
        Path currentTreeOutPath = outputFilePathMapper.apply(walkingPath);
        if (!JsonUtil.isEmpty(parentTree) && Files.notExists(currentTreeOutPath)) {
            logOperation("Flatten the tree `{}` into the file `{}`", walkingPath, currentTreeOutPath);
            Files.createDirectories(currentTreeOutPath.getParent());
            String json = GsonMapper.getGson().toJson(parentTree);
            Files.writeString(currentTreeOutPath, json);
            this.overrideTheOriginalFileWithSkeletonTree = true;
        }

        /* Remove all keys on leave. */
        parentTree.keySet()
            .stream()
            .toList()
            .forEach(parentTree::remove);
    }

    private JsonObject makeSkeletonTree(@NotNull JsonObject rootTree) {
        rootTree.keySet().stream().toList()
            .stream()
            .filter(key -> !key.equals(subtreeIdentifier))
            .forEach(key -> {
                if (rootTree.get(key).isJsonObject()) {
                    /* Go deeper. */
                    JsonObject subTree = rootTree.getAsJsonObject(key);
                    makeSkeletonTree(subTree);

                    /* Remove the empty subtree. */
                    if (JsonUtil.isEmpty(subTree)) {
                        rootTree.remove(key);
                    }
                } else {
                    rootTree.remove(key);
                }
            });
        return rootTree;
    }

    @Override
    public void apply() {
        DocumentContext context = this.getJsonDocumentContext();
        JsonObject rootTree = (JsonObject) getJsonPath(context, this.rootTreePath);
        this.flatten(rootTree, this.walkingPath);

        if (overrideTheOriginalFileWithSkeletonTree) {
            JsonObject skeletonTree = (JsonObject) getJsonPath(context, this.rootTreePath);
            setJsonPath(context, this.rootTreePath, makeSkeletonTree(skeletonTree));
            writeJsonDocumentContextToOriginalFile(context);
        }
    }

}
