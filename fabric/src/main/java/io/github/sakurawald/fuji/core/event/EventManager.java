package io.github.sakurawald.fuji.core.event;

import io.github.sakurawald.fuji.core.event.message.abst.BaseEvent;
import io.github.sakurawald.fuji.core.event.message.abst.BaseEventConsumer;
import io.github.sakurawald.fuji.core.event.inject.StaticEventConsumerInjector;
import io.github.sakurawald.fuji.core.manager.abst.BaseManager;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jetbrains.annotations.NotNull;

public class EventManager extends BaseManager {

    public static Map<Class<? extends BaseEvent>, List<BaseEventConsumer<?>>> events = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {
        StaticEventConsumerInjector.injectAll();
    }

    public static <T extends BaseEvent> void registerEventConsumer(@NotNull Class<T> eventType, @NotNull BaseEventConsumer<T> eventConsumer) {
        getEventConsumerList(eventType)
            .add(eventConsumer);

        getEventConsumerList(eventType)
            .sort(Comparator
                .comparing((BaseEventConsumer<?> it) -> it.getEventConsumerInfo().getInjectorPriority())
                .thenComparing(it -> it.getEventConsumerInfo().getConsumerPriority())
            );
    }

    @SuppressWarnings("unchecked")
    public static <T extends BaseEvent> void dispatchEvent(@NotNull Class<T> eventType, @NotNull T event, int eventInjectorPriority) {
        for (BaseEventConsumer<?> eventConsumer : getEventConsumerList(eventType)) {
            if (eventConsumer.getEventConsumerInfo().getInjectorPriority() != eventInjectorPriority) {
                continue;
            }

            BaseEventConsumer<T> $eventConsumer = (BaseEventConsumer<T>) eventConsumer;
            $eventConsumer.consumeEvent(event);
        }
    }

    private static @NotNull List<BaseEventConsumer<?>> getEventConsumerList(@NotNull Class<? extends BaseEvent> eventType) {
        return events
            .computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>());
    }

}
