package io.github.sakurawald.fuji.core.manager.impl.cache.structure;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Cache<T> {

    T value;
    long updatedTimestamp;

    public static <T> Cache<T> of(@NotNull T value) {
        Cache<T> result = new Cache<>();
        result.value = value;
        result.updatedTimestamp = System.currentTimeMillis();
        return result;
    }

}
