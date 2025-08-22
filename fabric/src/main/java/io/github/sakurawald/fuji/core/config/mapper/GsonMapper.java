package io.github.sakurawald.fuji.core.config.mapper;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import io.github.sakurawald.fuji.core.config.migrator.version.IgnoreModVersionStrategy;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import java.io.Reader;
import java.lang.reflect.Type;
import org.jetbrains.annotations.NotNull;

public class GsonMapper {

    private static Gson gson = new GsonBuilder()
        // The default naming policy is IDENTIFY, we need to ensure the naming style is consistent.
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        // Pretty print for readability.
        .setPrettyPrinting()
        // Pass through html characters, to support mini language.
        .disableHtmlEscaping()
        // Null-value is legal value, we should serialize it.
        .serializeNulls()
        // Exclude the mod version property in both serialization and de-serialization.
        .setExclusionStrategies(new IgnoreModVersionStrategy())
        // If the Gson can't find a no-args-constructor, then it will try to create an instance using Unsafe, and ignore all the declared field initializers.
        .disableJdkUnsafe()
        // Note that non-static inner class always holds a reference to its enclosing outer class. (Makes the no args constructor failed)
        .disableInnerClassSerialization()
        // Let's create it.
        .create();

    @ForDeveloper("I want a friend class, the package visibility is hard to use.")
    public static @NotNull Gson getInternalGsonReferenceWithoutTheUseOfWrappedFunctions() {
        return gson;
    }

    public static void registerGsonTypeAdapter(@NotNull Type type, @NotNull Object typeAdapter) {
        gson = gson
            .newBuilder()
            .registerTypeAdapter(type, typeAdapter)
            .create();
    }

    public static <T> @NotNull T fromJson(@NotNull Reader jsonReader, @NotNull Class<T> classOfT) {
        return gson.fromJson(jsonReader, classOfT);
    }

    public static <T> @NotNull T fromJson(@NotNull String json, @NotNull Class<T> classOfT) throws JsonSyntaxException {
        return gson.fromJson(json, classOfT);
    }

    public static <T> @NotNull T fromJson(@NotNull JsonElement jsonElement, @NotNull Class<T> classOfT) throws JsonSyntaxException {
        return gson.fromJson(jsonElement, classOfT);
    }

    public static @NotNull JsonElement toJsonTree(@NotNull Object src) {
        return gson.toJsonTree(src);
    }

    public static @NotNull String toJsonString(@NotNull Object src) {
        return gson.toJson(src);
    }

    public static @NotNull String toJsonString(@NotNull JsonElement jsonElement) {
        return gson.toJson(jsonElement);
    }

}
