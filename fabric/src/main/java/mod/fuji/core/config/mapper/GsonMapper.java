package mod.fuji.core.config.mapper;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Cleanup;
import lombok.SneakyThrows;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.StringUtil;
import mod.fuji.core.config.annotation.NotNullEnumType;
import mod.fuji.core.config.mapper.adapter.BiMapTypeAdapterFactory;
import mod.fuji.core.config.mapper.adapter.CheckedEnumTypeValueAdapterFactory;
import mod.fuji.core.config.mapper.wrapper.PropertyMapIR;
import mod.fuji.core.config.migrator.version.IgnoreModVersionFieldStrategy;
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
        .setExclusionStrategies(new IgnoreModVersionFieldStrategy())
        // If the Gson can't find a no-args-constructor, then it will try to create an instance using Unsafe, and ignore all the declared field initializers.
        .disableJdkUnsafe()
        // Note that non-static inner class always holds a reference to its enclosing outer class. (Makes the no args constructor failed)
        .disableInnerClassSerialization()
        // Register type adapters.
        .registerTypeAdapterFactory(new BiMapTypeAdapterFactory())
        .registerTypeAdapterFactory(new CheckedEnumTypeValueAdapterFactory())
        .registerTypeAdapter(PropertyMapIR.class, new PropertyMapIR.PropertyMapWrapperAdapter())
        // Let's create it.
        .create();

    private static final Map<Class<?>, Boolean> TYPE_NULLABILITY_MAP = new ConcurrentHashMap<>();

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

    /**
     * I want a friend class, the package visibility is hard to use.
     **/
    public static @NotNull Gson __GetInternalGsonReferenceWithoutTheUseOfWrappedFunctions() {
        return gson;
    }

    public static @NotNull FieldNamingStrategy getFieldNamingStrategy() {
        return gson.fieldNamingStrategy();
    }

    public static <T> TypeAdapter<T> getDelegateAdapter(TypeAdapterFactory skipPast, TypeToken<T> type) {
        return gson.getDelegateAdapter(skipPast, type);
    }

    /**
 *         The Gson library has already register a bunch of pre-defined type adapters.
        See: TypeAdapters

 **/
    public static void registerGsonTypeAdapter(@NotNull Type type, @NotNull Object typeAdapter) {
        gson = gson
            .newBuilder()
            .registerTypeAdapter(type, typeAdapter)
            .create();
    }

    public static void setTypeNullability(@NotNull Class<?> typeClass, boolean nullable) {
        TYPE_NULLABILITY_MAP.put(typeClass, nullable);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static boolean isNullableType(@NotNull Class<?> typeClass) {
        return TYPE_NULLABILITY_MAP.computeIfAbsent(typeClass, k -> {
            boolean nullable = Optional
                .ofNullable(typeClass.getAnnotation(NotNullEnumType.class))
                .isEmpty();
            return nullable;
        });
    }

    @SneakyThrows(IOException.class)
    public static <T> @NotNull T fromJson(@NotNull Path filePath, @NotNull TypeToken<T> runtimeType) {
        @Cleanup Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath.toFile()), StandardCharsets.UTF_8));
        try {
            return gson.fromJson(reader, runtimeType);
        } catch (JsonIOException e) {
            handleGsonJdkUnsafeDisabledException(runtimeType.getType(), e);
            return getFallbackGson().fromJson(reader, runtimeType);
        }
    }

    public static <T> @NotNull T fromJson(@NotNull Reader jsonReader, @NotNull Class<T> rawType) {
        try {
            return gson.fromJson(jsonReader, rawType);
        } catch (JsonIOException e) {
            handleGsonJdkUnsafeDisabledException(rawType, e);
            return getFallbackGson().fromJson(jsonReader, rawType);
        }
    }

    public static <T> @NotNull T fromJson(@NotNull String json, @NotNull Class<T> rawType) throws JsonSyntaxException {
        try {
            return gson.fromJson(json, rawType);
        } catch (JsonIOException e) {
            handleGsonJdkUnsafeDisabledException(rawType, e);
            return getFallbackGson().fromJson(json, rawType);
        }
    }

    public static <T> @NotNull T fromJson(@NotNull JsonElement jsonElement, @NotNull Class<T> rawType) throws JsonSyntaxException {
        try {
            return gson.fromJson(jsonElement, rawType);
        } catch (JsonIOException e) {
            handleGsonJdkUnsafeDisabledException(rawType, e);
            return getFallbackGson().fromJson(jsonElement, rawType);
        }
    }

    private static void handleGsonJdkUnsafeDisabledException(@NotNull Type type, JsonIOException e) {
        if (!StringUtil.toLowerCase(e.getMessage()).contains("jdk unsafe")) {
            throw e;
        }

        LogUtil.warn("""

            [JDK Unsafe feature is used to create a Java Object instance]
            ◉ Problem: Failed to create instance of {} using vanilla Java NoArgsConstructor, now falling back to use JDK Unsafe to create the instance.
            ◉ Solution: If you see this, you should create an issue in https://github.com/fuji-fabric/fuji/issues
            """, type);
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
