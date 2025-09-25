package mod.fuji.core.event;

import mod.fuji.core.annotation.HotPath;
import mod.fuji.core.event.message.BaseEvent;
import mod.fuji.core.event.consumer.EventConsumer;
import mod.fuji.core.event.injector.StaticEventConsumerInjector;
import mod.fuji.core.manager.abst.BaseManager;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public class EventManager extends BaseManager {

    private static final Map<Class<? extends BaseEvent>, List<EventConsumer<?>>> events = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {
        StaticEventConsumerInjector.injectAll();
    }

    public static <T extends BaseEvent> void registerEventConsumer(@NotNull Class<T> eventType, @NotNull EventConsumer<T> eventConsumer) {
        getEventConsumerList(eventType)
            .add(eventConsumer);

        getEventConsumerList(eventType)
            .sort(Comparator
                .comparing((EventConsumer<?> it) -> it.getEventConsumerInfo().getInjectorPriority())
                .thenComparing(it -> it.getEventConsumerInfo().getConsumerPriority())
            );
    }

    @SuppressWarnings("unchecked")
    @HotPath("This is the generic event dispatcher method.")
    public static <T extends BaseEvent> void dispatchEvent(@NotNull Class<T> eventType, @NotNull T event, int eventInjectorPriority) {
        for (EventConsumer<?> eventConsumer : getEventConsumerList(eventType)) {
            if (eventConsumer.getEventConsumerInfo().getInjectorPriority() != eventInjectorPriority) {
                continue;
            }

            EventConsumer<T> $eventConsumer = (EventConsumer<T>) eventConsumer;
            $eventConsumer.consumeEvent(event);
        }
    }

    private static @NotNull List<EventConsumer<?>> getEventConsumerList(@NotNull Class<? extends BaseEvent> eventType) {
        return events
            .computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>());
    }

    public static @Unmodifiable Map<Class<? extends BaseEvent>, List<EventConsumer<?>>> getEvents() {
        return Collections.unmodifiableMap(events);
    }

}
