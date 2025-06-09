package io.github.sakurawald.core.auxiliary;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class JsonUtil {

    @SuppressWarnings("RedundantIfStatement")
    public static boolean sameType(@NotNull JsonElement a, @NotNull JsonElement b) {
        if (a.isJsonObject() && b.isJsonObject()) return true;
        if (a.isJsonArray() && b.isJsonArray()) return true;
        if (a.isJsonNull() && b.isJsonNull()) return true;
        if (a.isJsonPrimitive() && b.isJsonPrimitive()) {
            JsonPrimitive ap = a.getAsJsonPrimitive();
            JsonPrimitive bp = b.getAsJsonPrimitive();

            if (ap.isString() && bp.isString()) return true;
            if (ap.isBoolean() && bp.isBoolean()) return true;
            if (ap.isNumber() && bp.isNumber()) return true;
        }

        return false;
    }

    public static boolean existsNode(@NotNull JsonObject root, @NotNull String path) {
        String[] nodes = path.split("\\.");
        for (int i = 0; i < nodes.length - 1; i++) {
            String node = nodes[i];
            if (!root.has(node)) return false;
            if (!root.isJsonObject()) return false;

            root = root.getAsJsonObject(node);
        }

        return root.has(nodes[nodes.length - 1]);
    }

    // The JsonObject.isEmpty() is not exist in old version gson, so the sinytra-connector will fail to load the mod.
    @SuppressWarnings("SizeReplaceableByIsEmpty")
    public static boolean isEmpty(JsonObject obt) {
        return obt.size() == 0;
    }
}
