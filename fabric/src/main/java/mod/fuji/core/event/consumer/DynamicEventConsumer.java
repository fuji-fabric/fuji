package mod.fuji.core.event.consumer;

import mod.fuji.core.auxiliary.ReflectionUtil;
import mod.fuji.core.document.annotation.ForDeveloper;
import mod.fuji.core.event.injector.structure.EventConsumerInfo;
import mod.fuji.core.event.message.BaseEvent;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

public class DynamicEventConsumer<T> extends EventConsumer<T> {

    protected DynamicEventConsumer(EventConsumerInfo eventConsumerInfo, Class<T> eventType, Consumer<T> eventConsumerMethod) {
        super(eventConsumerInfo, eventType, eventConsumerMethod);
    }

    @ForDeveloper("Make an event consumer at runtime programmatically.")
    public static <T extends BaseEvent> EventConsumer<T> makeDynamic(@NotNull Class<T> eventTypeClass, int injectorPriority, int consumerPriority, @NotNull Consumer<T> eventConsumer) {
        String eventTypeClassName = eventTypeClass.getName();
        StackTraceElement callerMethod = ReflectionUtil.Stacktrace.getCallerMethod();
        String declaringClassName = callerMethod.getClassName();
        String declaringMethodName = callerMethod.getMethodName();

        EventConsumerInfo eventConsumerInfo = new EventConsumerInfo(eventTypeClassName, declaringClassName, declaringMethodName, injectorPriority, consumerPriority, true);
        return new DynamicEventConsumer<>(eventConsumerInfo, eventTypeClass, eventConsumer);
    }

}
