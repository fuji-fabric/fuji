package io.github.sakurawald.fuji.core.event.inject;

import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.inject.structure.EventConsumerInfo;
import io.github.sakurawald.fuji.core.event.inject.structure.EventGraph;
import io.github.sakurawald.fuji.core.event.message.abst.BaseEvent;
import io.github.sakurawald.fuji.core.event.message.abst.BaseEventConsumer;
import io.github.sakurawald.fuji.core.event.message.abst.StaticEventConsumer;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleLoadDeterminer;
import java.lang.reflect.Method;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

public class StaticEventConsumerInjector {

    public static void injectAll() {
        EventGraph eventGraph = ReflectionUtil.CompileTimeGraph.getEventGraph();

        eventGraph
            .getConsumers()
            .forEach(StaticEventConsumerInjector::injectOne);
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows({ClassNotFoundException.class, NoSuchMethodException.class})
    private static <T extends BaseEvent> void injectOne(@NotNull EventConsumerInfo eventConsumerInfo) {
        /* Skip injecting, if its declaring class should not be loaded. */
        if (!ModuleLoadDeterminer.shouldLoadThis(eventConsumerInfo.getDeclaringClassName())) {
            return;
        }

        /* Skip injecting, if it's a dynamic event consumer. */
        if (eventConsumerInfo.isDynamic()) {
            return;
        }

        /* Inject this event consumer. */
        String eventTypeClassName = eventConsumerInfo.getEventTypeClassName();
        Class<T> eventTypeClass = (Class<T>) Class.forName(eventTypeClassName);

        Class<?> eventConsumerDeclaringClass = Class.forName(eventConsumerInfo.getDeclaringClassName());
        Method eventConsumerDeclaringMethod = eventConsumerDeclaringClass.getDeclaredMethod(eventConsumerInfo.getDeclaringMethodName(), eventTypeClass);

        BaseEventConsumer<T> baseEventConsumer = StaticEventConsumer.makeStatic(eventConsumerInfo, eventTypeClass, eventConsumerDeclaringMethod);
        EventManager.registerEventConsumer(eventTypeClass, baseEventConsumer);
    }

}
