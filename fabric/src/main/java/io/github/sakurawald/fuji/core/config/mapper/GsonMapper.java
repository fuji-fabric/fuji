package io.github.sakurawald.fuji.core.config.mapper;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.StringUtil;
import io.github.sakurawald.fuji.core.config.migrator.version.IgnoreModVersionStrategy;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import java.io.Reader;
import java.lang.reflect.Field;
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

    private static @NotNull Gson getFallbackGson() {
        GsonBuilder fallbackGsonBuilder = gson.newBuilder();
        enableJdkUnsafeFeature(fallbackGsonBuilder);
        return fallbackGsonBuilder.create();
    }

    private static void enableJdkUnsafeFeature(@NotNull GsonBuilder builder) {
        try {
            // NOTE: The `useJdkUnsafe` field name is stable since Gson v2.9.0 version.
            Field field = GsonBuilder.class.getDeclaredField("useJdkUnsafe");
            field.setAccessible(true);
            field.setBoolean(builder, true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to enable JDK Unsafe Feature in GsonBuilder.", e);
        }
    }

    @ForDeveloper("I want a friend class, the package visibility is hard to use.")
    public static @NotNull Gson __GetInternalGsonReferenceWithoutTheUseOfWrappedFunctions() {
        return gson;
    }

    public static void registerGsonTypeAdapter(@NotNull Type type, @NotNull Object typeAdapter) {
        gson = gson
            .newBuilder()
            .registerTypeAdapter(type, typeAdapter)
            .create();
    }

    public static <T> @NotNull T fromJson(@NotNull Reader jsonReader, @NotNull Class<T> classOfT) {
        try {
            return gson.fromJson(jsonReader, classOfT);
        } catch (JsonIOException e) {
            handleGsonJdkUnsafeDisabledException(classOfT, e);
            return getFallbackGson().fromJson(jsonReader, classOfT);
        }
    }

    public static <T> @NotNull T fromJson(@NotNull String json, @NotNull Class<T> classOfT) throws JsonSyntaxException {
        try {
            return gson.fromJson(json, classOfT);
        } catch (JsonIOException e) {
            handleGsonJdkUnsafeDisabledException(classOfT, e);
            return getFallbackGson().fromJson(json, classOfT);
        }
    }

    public static <T> @NotNull T fromJson(@NotNull JsonElement jsonElement, @NotNull Class<T> classOfT) throws JsonSyntaxException {
        try {
            return gson.fromJson(jsonElement, classOfT);
        } catch (JsonIOException e) {
            handleGsonJdkUnsafeDisabledException(classOfT, e);
            return getFallbackGson().fromJson(jsonElement, classOfT);
        }
    }

    private static <T> void handleGsonJdkUnsafeDisabledException(@NotNull Class<T> classOfT, JsonIOException e) {
        if (!StringUtil.toLowerCase(e.getMessage()).contains("jdk unsafe")) {
            throw e;
        }

        LogUtil.warn("""

            [JDK Unsafe feature is used to create a Java Object instance]
            ◉ Problem: Failed to create instance of {} using vanilla Java NoArgsConstructor, now falling back to use JDK Unsafe to create the instance.
            ◉ Solution: If you see this, you should create an issue in https://github.com/sakurawald/fuji/issues
            """, classOfT);
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
