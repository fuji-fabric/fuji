package io.github.sakurawald.fuji.core.structure;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Cooldown<T> {

    final Map<T, Long> timestamp = new HashMap<>();

    public long getRemainingTime(T key, Long cooldownPeriod) {
        long lastUseTime = this.getLastUseTime(key);
        long currentTime = System.currentTimeMillis();
        return cooldownPeriod - (currentTime - lastUseTime);
    }

    public long getLastUseTime(T key) {
       return timestamp.computeIfAbsent(key, k -> 0L);
    }

    public long tryUse(T key, Long cooldown) {
        long remainingTime = getRemainingTime(key, cooldown);

        if (remainingTime < 0) {
            onUse(key);
        }

        return remainingTime;
    }

    private void onUse(T key) {
        timestamp.put(key, System.currentTimeMillis());
    }

}
