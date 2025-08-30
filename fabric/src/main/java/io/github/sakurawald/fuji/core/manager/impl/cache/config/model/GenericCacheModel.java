package io.github.sakurawald.fuji.core.manager.impl.cache.config.model;

import io.github.sakurawald.fuji.core.manager.impl.cache.structure.Cache;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GenericCacheModel<T> {

    ConcurrentHashMap<String, Cache<T>> cacheMap = new ConcurrentHashMap<>();

    transient boolean dirty;
}
