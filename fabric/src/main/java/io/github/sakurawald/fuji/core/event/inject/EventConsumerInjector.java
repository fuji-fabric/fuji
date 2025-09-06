package io.github.sakurawald.fuji.core.event.inject;

import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.message.abst.BaseEvent;
import io.github.sakurawald.fuji.core.event.message.abst.BaseEventConsumer;
import io.github.sakurawald.fuji.core.event.inject.structure.EventConsumerInfo;
import io.github.sakurawald.fuji.core.event.inject.structure.EventGraph;
import io.github.sakurawald.fuji.core.event.message.abst.StaticEventConsumer;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import java.lang.reflect.Method;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

public class EventConsumerInjector {

    public static void injectAll() {
        EventGraph eventGraph = ReflectionUtil.CompileTimeGraph.getEventGraph();

        eventGraph
            .getConsumers()
            .forEach(EventConsumerInjector::inject);
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows(ClassNotFoundException.class)
    private static void inject(@NotNull EventConsumerInfo eventConsumerInfo) {
        /* Ignore this event consumer, if its declaring class should not be loaded. */
        if (!ModuleManager.shouldLoadThis(eventConsumerInfo.getDeclaringClassName())) {
            return;
        }

        /* Inject this event consumer. */
        String eventTypeClassName = eventConsumerInfo.getEventTypeClassName();
        Class<? extends BaseEvent> eventTypeClass = (Class<? extends BaseEvent>) Class.forName(eventTypeClassName);
        inject(eventTypeClass, eventConsumerInfo);
    }

    @SneakyThrows({ClassNotFoundException.class, NoSuchMethodException.class})
    private static <T extends BaseEvent> void inject(@NotNull Class<T> eventTypeClass, @NotNull EventConsumerInfo eventConsumerInfo) {
        if (eventConsumerInfo.isStatic()) {
            Class<?> eventConsumerDeclaringClass = Class.forName(eventConsumerInfo.getDeclaringClassName());
            Method eventConsumerDeclaringMethod = eventConsumerDeclaringClass.getDeclaredMethod(eventConsumerInfo.getDeclaringMethodName(), eventTypeClass);

            BaseEventConsumer<T> baseEventConsumer = StaticEventConsumer.makeStatic(eventConsumerInfo, eventTypeClass, eventConsumerDeclaringMethod);
            EventManager.registerEventConsumer(eventTypeClass, baseEventConsumer);
        }
    }

}
