package mod.fuji.core.service.cache.config.model;

import mod.fuji.core.service.cache.structure.Cache;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GenericCacheModel<T> {

    ConcurrentHashMap<String, Cache<T>> cacheMap = new ConcurrentHashMap<>();

    transient boolean dirty;
}
