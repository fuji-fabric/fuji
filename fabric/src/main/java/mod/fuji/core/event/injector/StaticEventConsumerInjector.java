package mod.fuji.core.event.injector;

import mod.fuji.core.auxiliary.ReflectionUtil;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.injector.structure.EventConsumerInfo;
import mod.fuji.core.event.injector.structure.EventGraph;
import mod.fuji.core.event.injector.structure.EventProducerInfo;
import mod.fuji.core.event.message.BaseEvent;
import mod.fuji.core.event.consumer.BaseEventConsumer;
import mod.fuji.core.event.consumer.StaticEventConsumer;
import mod.fuji.core.manager.impl.module.ModuleLoadDeterminer;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

public class StaticEventConsumerInjector {

    @Getter(lazy = true)
    private static final Set<String> eventProducerMixinClassNames = collectEventProducerMixinClassNames();

    public static void injectAll() {
        EventGraph eventGraph = ReflectionUtil.CompileTimeGraph.getEventGraph();

        eventGraph
            .getConsumers()
            .forEach(StaticEventConsumerInjector::injectOne);
    }

    public static @NotNull Set<String> collectEventProducerMixinClassNames() {
        EventGraph eventGraph = ReflectionUtil.CompileTimeGraph.getEventGraph();
        return eventGraph
            .getProducers()
            .stream()
            .map(EventProducerInfo::getDeclaringClassName)
            .collect(Collectors.toSet());
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
