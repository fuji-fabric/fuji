package mod.fuji.core.job;

import java.util.concurrent.atomic.AtomicReference;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import mod.fuji.core.annotation.Unused;
import mod.fuji.core.auxiliary.ExceptionUtil;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import mod.fuji.core.event.message.server.lifecycle.ServerStoppingEvent;
import org.jetbrains.annotations.NotNull;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

public class GlobalScheduler {

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private static final AtomicReference<Scheduler> schedulerRef = initializeSchedulerReference();

    private static @NotNull Scheduler makeSchedulerInstance() {
        try {
            // NOTE: The scheduled jobs are associated with the scheduler ID, not the scheduler instance.
            // A job is stored in JobStore, not the Scheduler instance itself.
            StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
            return stdSchedulerFactory.getScheduler();
        } catch (SchedulerException e) {
            throw ExceptionUtil.makeReThrownException(e);
        }
    }

    private static AtomicReference<Scheduler> initializeSchedulerReference() {
        Scheduler defaultValue = makeSchedulerInstance();
        return new AtomicReference<>(defaultValue);
    }

    private static void resetSchedulerReference() {
        Scheduler newValue = makeSchedulerInstance();
        getSchedulerRef().set(newValue);
    }

    @SneakyThrows(SchedulerException.class)
    public static @NotNull Scheduler getInstance() {
        Scheduler scheduler = getSchedulerRef().get();
        if (scheduler.isShutdown()) {
            resetSchedulerReference();
            return getSchedulerRef().get();
        }
        return scheduler;
    }

    @EventConsumer(injectorPriority = EventConsumer.HIGHEST, consumerPriority = EventConsumer.HIGHEST)
    private static void startScheduler(@Unused ServerStartedEvent event) {
        try {
            getInstance().start();
        } catch (SchedulerException e) {
            LogUtil.error("Failed to start the scheduler.", e);
        }
    }

    @EventConsumer
    private static void shutdownScheduler(@Unused ServerStoppingEvent event) {
        try {
            // NOTE: The shutdown method will return immediately, the executing jobs will continue running to completion.
            getInstance().shutdown(false);
        } catch (SchedulerException e) {
            LogUtil.error("Failed to shutdown the scheduler", e);
        }
    }

}
