package io.github.sakurawald.fuji.core.event.consumer;

import io.github.sakurawald.fuji.core.event.injector.structure.EventConsumerInfo;
import io.github.sakurawald.fuji.core.event.message.BaseEvent;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

public class StaticEventConsumer<T> extends BaseEventConsumer<T> {

    private StaticEventConsumer(EventConsumerInfo eventConsumerInfo, Class<T> eventType, Consumer<T> consumer) {
        super(eventConsumerInfo, eventType, consumer);
    }

    @SneakyThrows(Throwable.class)
    @SuppressWarnings("unchecked")
    private static <E> @NotNull Consumer<E> toConsumer(@NotNull Class<E> eventType, @NotNull Method method) {
        method.setAccessible(true);
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup());
        MethodHandle handle = lookup.unreflect(method);

        MethodType factoryType = MethodType.methodType(Consumer.class);
        MethodType methodType = MethodType.methodType(void.class, eventType);

        CallSite site = LambdaMetafactory.metafactory(
            lookup,
            "accept",
            factoryType,
            MethodType.methodType(void.class, Object.class),
            handle,
            methodType
        );

        return (Consumer<E>) site.getTarget().invoke();
    }

    public static <T extends BaseEvent> BaseEventConsumer<T> makeStatic(@NotNull EventConsumerInfo eventConsumerInfo, @NotNull Class<T> eventType, @NotNull Method eventConsumerMethod) {
        Consumer<T> consumer = toConsumer(eventType, eventConsumerMethod);
        return new StaticEventConsumer<>(eventConsumerInfo, eventType, consumer);
    }

}
