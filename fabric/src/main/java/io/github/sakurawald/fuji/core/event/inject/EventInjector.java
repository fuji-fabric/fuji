package io.github.sakurawald.fuji.core.event.inject;

import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.abst.BaseEvent;
import io.github.sakurawald.fuji.core.event.abst.BaseEventConsumer;
import io.github.sakurawald.fuji.core.event.inject.structure.EventConsumerInfo;
import io.github.sakurawald.fuji.core.event.inject.structure.EventConsumerInfoList;
import io.github.sakurawald.fuji.core.event.inject.structure.EventGraph;
import java.lang.reflect.Method;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

public class EventInjector {

    public static void injectAll() {
        String eventGraphFileName = ReflectionUtil.CompileTimeGraph.EVENT_GRAPH_FILE_NAME;
        EventGraph eventGraph = ReflectionUtil.CompileTimeGraph.getCompileTimeJsonGraph(eventGraphFileName, EventGraph.class);

        eventGraph
            .getConsumers()
            .forEach(EventInjector::inject);
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows(ClassNotFoundException.class)
    private static void inject(@NotNull String eventTypeClassName, @NotNull EventConsumerInfoList eventConsumerInfoList) {
        Class<? extends BaseEvent> eventTypeClass = (Class<? extends BaseEvent>) Class.forName(eventTypeClassName);
        eventConsumerInfoList
            .forEach(eventConsumerInfo -> inject(eventTypeClass, eventConsumerInfo));
    }

    @SneakyThrows({ClassNotFoundException.class, NoSuchMethodException.class})
    private static <T extends BaseEvent> void inject(@NotNull Class<T> eventTypeClass, @NotNull EventConsumerInfo eventConsumerInfo) {
        Class<?> eventConsumerDeclaringClass = Class.forName(eventConsumerInfo.getDeclaringClassName());
        Method eventConsumerDeclaringMethod = eventConsumerDeclaringClass.getDeclaredMethod(eventConsumerInfo.getDeclaringMethodName(), eventTypeClass);

        BaseEventConsumer<T> baseEventConsumer = new BaseEventConsumer<>(eventTypeClass, eventConsumerDeclaringMethod);
        EventManager.registerEventConsumer(eventTypeClass, baseEventConsumer);
    }

}
