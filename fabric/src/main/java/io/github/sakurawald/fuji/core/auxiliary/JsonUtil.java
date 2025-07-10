package io.github.sakurawald.fuji.core.auxiliary;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

public class JsonUtil {

    @SuppressWarnings("RedundantIfStatement")
    public static boolean equalsJsonElementType(@NotNull JsonElement a, @NotNull JsonElement b) {
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
        /* Split the path into keys. */
        String[] nodes = path.split("\\.");

        /* Walk the path along the keys. (Exclude the last key) */
        for (int i = 0; i < nodes.length - 1; i++) {
            String node = nodes[i];
            if (!root.has(node)) return false;
            if (!root.isJsonObject()) return false;

            root = root.getAsJsonObject(node);
        }

        /* Check the last key. */
        String theLastKey = nodes[nodes.length - 1];
        return root.has(theLastKey);
    }

    @SuppressWarnings("SizeReplaceableByIsEmpty")
    public static boolean isEmpty(JsonObject obj) {
        // NOTE: The JsonObject.isEmpty() is not exist in old version gson, so the sinytra-connector will fail to load the mod.
        return obj.size() == 0;
    }

    @SneakyThrows(IOException.class)
    public static JsonElement readJsonElement(Path path) {
        @Cleanup Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path.toFile())));
        return JsonParser.parseReader(reader);
    }
}
