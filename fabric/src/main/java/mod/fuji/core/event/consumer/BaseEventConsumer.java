package mod.fuji.core.event.consumer;

import mod.fuji.core.event.injector.structure.EventConsumerInfo;
import java.util.function.Consumer;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class BaseEventConsumer<T> {

    @NotNull EventConsumerInfo eventConsumerInfo;

    @NotNull Class<T> eventType;

    @NotNull Consumer<T> eventConsumerMethod;

    protected BaseEventConsumer(@NotNull EventConsumerInfo eventConsumerInfo, @NotNull Class<T> eventType, @NotNull Consumer<T> eventConsumerMethod) {
        this.eventConsumerInfo = eventConsumerInfo;
        this.eventType = eventType;
        this.eventConsumerMethod = eventConsumerMethod;
    }

    public void consumeEvent(@NotNull T event) {
        this.eventConsumerMethod.accept(event);
    }

}
