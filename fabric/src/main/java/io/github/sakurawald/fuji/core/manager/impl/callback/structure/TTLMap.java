package io.github.sakurawald.fuji.core.manager.impl.callback.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.job.abst.CronJob;
import io.github.sakurawald.fuji.core.manager.impl.scheduler.ScheduleManager;
import lombok.NoArgsConstructor;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class TTLMap<K, V> {
    private final ConcurrentMap<K, ExpiringValue<V>> map = new ConcurrentHashMap<>();

    public TTLMap() {
        // do the cleanup every minute.
        new CleanTTLMapJob(new JobDataMap() {
            {
                this.put(TTLMap.class.getName(), TTLMap.this);
            }
        }, () -> ScheduleManager.CRON_EVERY_MINUTE).schedule();
    }

    public void put(K key, V value, long ttl, TimeUnit unit) {
        long expiryTime = System.currentTimeMillis() + unit.toMillis(ttl);
        map.put(key, new ExpiringValue<>(value, expiryTime));
    }

    public V get(K key) {
        ExpiringValue<V> expiringValue = map.get(key);
        if (expiringValue != null && System.currentTimeMillis() < expiringValue.expiryTime) {
            return expiringValue.value;
        } else {
            map.remove(key);
            return null;
        }
    }

    public void remove(K key) {
        map.remove(key);
    }

    private void cleanUp() {
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<K, ExpiringValue<V>> entry : map.entrySet()) {
            if (currentTime > entry.getValue().expiryTime) {
                map.remove(entry.getKey());
            }
        }
    }

    private record ExpiringValue<V>(V value, long expiryTime) {
    }


    @Document("""
        This `job` is used to clean the `map` data structure, based on `TTL`.
        """)
    @NoArgsConstructor
    public static class CleanTTLMapJob extends CronJob {

        public CleanTTLMapJob(JobDataMap jobDataMap, Supplier<String> cronSupplier) {
            super(jobDataMap, cronSupplier);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void execute(JobExecutionContext context) {
            TTLMap instance = (TTLMap) context.getJobDetail().getJobDataMap().get(TTLMap.class.getName());
            instance.cleanUp();
        }
    }
}
