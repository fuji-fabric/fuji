package io.github.sakurawald.core.structure;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Cooldown<T> {

    final Map<T, Long> timestamp = new HashMap<>();

    public long getCooldown(T key, Long cooldown) {
        long lastUpdateTimeMs = timestamp.computeIfAbsent(key, k -> 0L);
        long currentTimeMs = System.currentTimeMillis();
        long cooldownMS = cooldown;
        return cooldownMS - (currentTimeMs - lastUpdateTimeMs);
    }

    public long tryUse(T key, Long cooldown) {
        long leftTime = getCooldown(key, cooldown);
        if (leftTime < 0) {
            timestamp.put(key, System.currentTimeMillis());
        }

        return leftTime;
    }

}
