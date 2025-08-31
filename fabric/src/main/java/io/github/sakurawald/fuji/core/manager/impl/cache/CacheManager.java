package io.github.sakurawald.fuji.core.manager.impl.cache;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.auxiliary.JsonUtil;
import io.github.sakurawald.fuji.core.config.mapper.GsonMapper;
import io.github.sakurawald.fuji.core.event.impl.PlayerEvents;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.manager.abst.BaseManager;
import io.github.sakurawald.fuji.core.manager.impl.cache.config.model.GenericCacheModel;
import io.github.sakurawald.fuji.core.manager.impl.cache.job.FlushCacheJob;
import io.github.sakurawald.fuji.core.manager.impl.cache.service.GameProfileCacheService;
import io.github.sakurawald.fuji.core.manager.impl.cache.structure.Cache;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

public class CacheManager extends BaseManager {

    private static final Path CACHE_DIRECTORY = Fuji.MOD_CONFIG_PATH.resolve("cache");
    private static final Map<String, GenericCacheModel<?>> CACHE_FILES = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Managers.getScheduleManager().scheduleJob(new FlushCacheJob());
        });

        PlayerEvents.ON_PLAYER_JOINED.register(GameProfileCacheService::setGameProfileCache);
    }

    @SneakyThrows(IOException.class)
    private @NotNull Path toCacheFilePath(@NotNull String cacheSubject) {
        Files.createDirectories(CACHE_DIRECTORY.getParent());
        return CACHE_DIRECTORY.resolve(cacheSubject + ".json");
    }

    public <T> @NotNull T getCachedValueOrCompute(@NotNull String cacheSubject, @NotNull String cacheKey, @NotNull Class<T> typeOfCacheValue, @NotNull Duration expirationDuration, @NotNull Supplier<T> supplier) {
        GenericCacheModel<T> model = getGenericCacheModel(cacheSubject, typeOfCacheValue);

        /* Get or create cache. */
        Cache<T> cache = model
            .getCacheMap()
            .computeIfAbsent(cacheKey, (key) -> {
                Cache<T> newValue = Cache.of(supplier.get());
                model.setDirty(true);
                return newValue;
            });

        /* Invalidate the cache by time. */
        long currentTime = System.currentTimeMillis();
        if (cache.getUpdatedTimestamp() + expirationDuration.toMillis() < currentTime) {
            cache.setValue(supplier.get());
            cache.setUpdatedTimestamp(currentTime);
            model.setDirty(true);
        }

        /* Return the cache value. */
        return cache.getValue();
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

    public void flushGenericCacheModels() {
        CACHE_FILES.forEach((cacheSubject, cacheModel) -> {
            if (!cacheModel.isDirty()) return;

            Path cacheFilePath = toCacheFilePath(cacheSubject);
            JsonObject jsonObject = GsonMapper.toJsonTree(cacheModel).getAsJsonObject();
            JsonUtil.writeJsonObject(jsonObject, cacheFilePath);
            cacheModel.setDirty(false);
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
