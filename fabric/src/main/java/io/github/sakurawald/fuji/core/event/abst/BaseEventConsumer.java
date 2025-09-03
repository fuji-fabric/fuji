package io.github.sakurawald.fuji.core.event.abst;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import lombok.Data;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

@Data
public class BaseEventConsumer<T> {

//    int priority;

    Class<T> eventType;

    Consumer<T> eventConsumerMethod;

    public BaseEventConsumer(@NotNull Class<T> eventType, @NotNull Method eventConsumerMethod) {
        this.eventType = eventType;
        this.eventConsumerMethod = toConsumer(this.eventType, eventConsumerMethod);
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

    public void handleEvent(T event) {
        this.eventConsumerMethod.accept(event);
    }

}
