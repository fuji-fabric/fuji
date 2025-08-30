package io.github.sakurawald.fuji.core.manager.impl.cache.config.model;

import io.github.sakurawald.fuji.core.manager.impl.cache.structure.Cache;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GenericCacheModel<T> {

    Map<String, Cache<T>> cacheMap = new HashMap<>();

    transient boolean dirty;
}
