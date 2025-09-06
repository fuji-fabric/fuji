package io.github.sakurawald.fuji.core.event.message.abst;

import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.event.inject.structure.EventConsumerInfo;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

public class DynamicEventConsumer<T> extends BaseEventConsumer<T> {

    protected DynamicEventConsumer(EventConsumerInfo eventConsumerInfo, Class<T> eventType, Consumer<T> eventConsumerMethod) {
        super(eventConsumerInfo, eventType, eventConsumerMethod);
    }

    @ForDeveloper("Make an event consumer at runtime programmatically.")
    public static <T extends BaseEvent> BaseEventConsumer<T> makeDynamic(@NotNull Class<T> eventTypeClass, int injectorPriority, int consumerPriority, @NotNull Consumer<T> eventConsumer) {
        String eventTypeClassName = eventTypeClass.getName();
        StackTraceElement callerMethod = ReflectionUtil.Stacktrace.getCallerMethod();
        String declaringClassName = callerMethod.getClassName();
        String declaringMethodName = callerMethod.getMethodName();

        EventConsumerInfo eventConsumerInfo = new EventConsumerInfo(eventTypeClassName, declaringClassName, declaringMethodName, injectorPriority, consumerPriority);
        return new DynamicEventConsumer<>(eventConsumerInfo, eventTypeClass, eventConsumer);
    }

}
