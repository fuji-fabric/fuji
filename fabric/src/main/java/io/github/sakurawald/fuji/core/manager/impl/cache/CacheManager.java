package io.github.sakurawald.fuji.core.manager.impl.cache;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.auxiliary.JsonUtil;
import io.github.sakurawald.fuji.core.config.mapper.GsonMapper;
import io.github.sakurawald.fuji.core.manager.abst.BaseManager;
import io.github.sakurawald.fuji.core.manager.impl.cache.config.model.GenericCacheModel;
import io.github.sakurawald.fuji.core.manager.impl.cache.structure.Cache;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

public class CacheManager extends BaseManager {

    private static final Path CACHE_DIRECTORY = Fuji.MOD_CONFIG_PATH.resolve("cache");
    private static final Map<String, GenericCacheModel<?>> CACHE_FILES = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {}

    @SneakyThrows
    private @NotNull Path toCacheFilePath(@NotNull String cacheSubject) {
        Files.createDirectories(CACHE_DIRECTORY.getParent());
        return CACHE_DIRECTORY.resolve(cacheSubject + ".json");
    }

    public <T> Optional<Cache<T>> getCache(@NotNull String cacheSubject, @NotNull String cacheKey, @NotNull Class<T> typeOfCacheValue) {
        GenericCacheModel<T> model = getGenericCacheModel(cacheSubject, typeOfCacheValue);

        return Optional
            .ofNullable(model
                .getCacheMap()
                .get(cacheKey));
    }

    @SuppressWarnings("unchecked")
    public <T> void setCache(@NotNull String cacheSubject, @NotNull String cacheKey, @NotNull T cacheValue) {
        GenericCacheModel<T> model = (GenericCacheModel<T>) getGenericCacheModel(cacheSubject, cacheValue.getClass());

        Cache<T> newCache = Cache.of(cacheValue);
        model.getCacheMap().put(cacheKey, newCache);
    }

    @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
    private <T> @NotNull GenericCacheModel<T> getGenericCacheModel(@NotNull String cacheSubject, @NotNull Class<T> typeOfValue) {
        GenericCacheModel<T> model = (GenericCacheModel<T>) CACHE_FILES.computeIfAbsent(cacheSubject, key -> {
            Path cacheFilePath = toCacheFilePath(cacheSubject);
            writeDefaultCacheModelIfAbsent(cacheFilePath);

            TypeToken<GenericCacheModel<T>> typeToken = (TypeToken<GenericCacheModel<T>>) TypeToken.getParameterized(GenericCacheModel.class, typeOfValue);
            return GsonMapper.fromJson(cacheFilePath, typeToken);
        });
        return model;
    }

    public void writeGenericCacheModels() {
        CACHE_FILES.forEach((cacheSubject, cacheModel) -> {
            Path cacheFilePath = toCacheFilePath(cacheSubject);
            JsonObject jsonObject = GsonMapper.toJsonTree(cacheModel).getAsJsonObject();
            JsonUtil.writeJsonObject(jsonObject, cacheFilePath);
        });
    }

    private static void writeDefaultCacheModelIfAbsent(@NotNull Path cacheFilePath) {
        if (!Files.exists(cacheFilePath)) {
            GenericCacheModel<?> defaultModel = new GenericCacheModel<>();
            JsonElement jsonTree = GsonMapper.toJsonTree(defaultModel);
            JsonUtil.writeJsonObject(jsonTree.getAsJsonObject(), cacheFilePath);
        }
    }
}
