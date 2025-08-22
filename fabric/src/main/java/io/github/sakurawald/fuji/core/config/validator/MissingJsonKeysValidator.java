package io.github.sakurawald.fuji.core.config.validator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import org.jetbrains.annotations.NotNull;

public class MissingJsonKeysValidator {

    public static void mergeTrees(@NotNull BaseConfigurationHandler<?> handler, @NotNull JsonObject dataTree, @NotNull JsonObject schemaTree) {
        schemaTree
            .keySet()
            .stream()
            .filter(key -> !dataTree.has(key))
            .forEach(key -> {
                LogUtil.debug("Add missing configuration key `{}` to file `{}`", key, handler.getFilePath());
                JsonElement value = schemaTree.get(key);
                dataTree.add(key, value);
            });
    }
}
