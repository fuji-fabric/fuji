package mod.fuji.core.event;

import mod.fuji.core.annotation.HotPath;
import mod.fuji.core.event.message.BaseEvent;
import mod.fuji.core.event.consumer.BaseEventConsumer;
import mod.fuji.core.event.injector.StaticEventConsumerInjector;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import mod.fuji.core.lifecycle.interfaces.ModSubInitializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public class EventManager implements ModSubInitializer {

    private static final Map<Class<? extends BaseEvent>, List<BaseEventConsumer<?>>> events = new ConcurrentHashMap<>();

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
    @HotPath("This is the generic event dispatcher method.")
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

    public static @Unmodifiable Map<Class<? extends BaseEvent>, List<BaseEventConsumer<?>>> getEvents() {
        return Collections.unmodifiableMap(events);
    }

}
