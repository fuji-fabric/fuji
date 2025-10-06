package mod.fuji.core.service.command_callback.structure;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.job.abst.CronJob;
import mod.fuji.core.job.JobManager;
import lombok.NoArgsConstructor;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class TTLMap<K, V> {
    private final ConcurrentMap<K, ExpiringValue<V>> backendMap = new ConcurrentHashMap<>();

    public TTLMap() {
        scheduleTTLMapCleanerJob();
    }

    private void scheduleTTLMapCleanerJob() {
        CleanTTLMapJob cleanTTLMapJob = new CleanTTLMapJob(new JobDataMap() {
            {
                this.put(TTLMap.class.getName(), TTLMap.this);
            }
        }, () -> JobManager.CRON_EVERY_MINUTE);
        JobManager.addJob(cleanTTLMapJob);
    }

    public void put(K key, V value, long ttl, TimeUnit unit) {
        long expiryTime = System.currentTimeMillis() + unit.toMillis(ttl);
        backendMap.put(key, new ExpiringValue<>(value, expiryTime));
    }

    public V get(K key) {
        ExpiringValue<V> expiringValue = backendMap.get(key);
        if (expiringValue != null && System.currentTimeMillis() < expiringValue.expiryTime) {
            return expiringValue.value;
        } else {
            backendMap.remove(key);
            return null;
        }
    }

    public void remove(K key) {
        backendMap.remove(key);
    }

    private void cleanUp() {
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<K, ExpiringValue<V>> entry : backendMap.entrySet()) {
            if (currentTime > entry.getValue().expiryTime) {
                backendMap.remove(entry.getKey());
            }
        }
    }

    private record ExpiringValue<V>(V value, long expiryTime) {}

    @Document(id = 1751823961060L, value = """
        This `job` is used to clean up the `TTL Map` data structure, and remove `expired entries`.
        The `TTL Map` is used in `/command-callback` command, to store the `callback entry`.

        <green>NOTE: The `/command-callback` command is typically used for `click event` in text.
        <green>A player requires the permission to use `/command-callback` command, or the client will get the `Unknown Command Error`.
        """)
    @NoArgsConstructor
    public static class CleanTTLMapJob extends CronJob {

        public CleanTTLMapJob(JobDataMap jobDataMap, Supplier<String> cronSupplier) {
            super(null, null, jobDataMap, cronSupplier, true);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void execute(JobExecutionContext context) {
            TTLMap instance = (TTLMap) context.getJobDetail().getJobDataMap().get(TTLMap.class.getName());
            instance.cleanUp();
        }
    }
}
