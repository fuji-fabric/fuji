package mod.fuji.core.auxiliary;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import mod.fuji.core.config.mapper.GsonMapper;
import mod.fuji.core.config.parser.JsonPathParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

public class JsonUtil {

    @SuppressWarnings("RedundantIfStatement")
    public static boolean equalsJsonElementType(@NotNull JsonElement a, @NotNull JsonElement b) {
        if (a.isJsonObject() && b.isJsonObject()) return true;
        if (a.isJsonArray() && b.isJsonArray()) return true;
        if (a.isJsonNull() && b.isJsonNull()) return true;
        if (a.isJsonPrimitive() && b.isJsonPrimitive()) {
            JsonPrimitive pa = a.getAsJsonPrimitive();
            JsonPrimitive pb = b.getAsJsonPrimitive();

            if (pa.isString() && pb.isString()) return true;
            if (pa.isBoolean() && pb.isBoolean()) return true;
            if (pa.isNumber() && pb.isNumber()) return true;
        }

        return false;
    }

    public static boolean existsNode(@NotNull JsonObject root, @NotNull String path) {
        /* Split the path into keys. */
        String[] nodes = path.split("\\.", -1);
        if (nodes.length == 0) return false;

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

    /**
 * The JsonObject.isEmpty() is not exist in old version gson, so the sinytra-connector will fail to load the mod.
 **/
    @SuppressWarnings("SizeReplaceableByIsEmpty")
    public static boolean isEmpty(JsonObject obj) {
        return obj.size() == 0;
    }

    public static @NotNull JsonObject readJsonFile(@NotNull Path path) {
        return GsonMapper.fromJson(path, TypeToken.get(JsonElement.class)).getAsJsonObject();
    }

    public static @NotNull JsonObject readJsonString(@NotNull String jsonString) {
        return GsonMapper.fromJson(jsonString, JsonElement.class).getAsJsonObject();
    }

    @SuppressWarnings("TypeParameterUnusedInFormals")
    public static <T> Optional<T> readJsonPath(@NotNull JsonObject root, @NotNull String path) {
        try {
            return Optional
                .ofNullable(JsonPathParser.getJsonPathParser()
                .parse(root)
                .read(path));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static <T extends JsonElement> void ifJsonElementPresent(@NotNull JsonObject root, @NotNull String key, @NotNull Class<T> expectedJsonElementType, @NotNull Consumer<T> consumer) {
        Optional
            .ofNullable(root.get(key))
            .ifPresent(jsonElement -> {
                if (expectedJsonElementType.isInstance(jsonElement)) {
                    T value = expectedJsonElementType.cast(jsonElement);
                    consumer.accept(value);
                }
            });
    }

    @SneakyThrows(IOException.class)
    public static void writeJsonObject(@NotNull JsonObject jsonObject, @NotNull Path outputFilePath){
        Files.createDirectories(outputFilePath.getParent());
        String json = GsonMapper.toJsonString(jsonObject);
        Files.writeString(outputFilePath, json);
    }
}
