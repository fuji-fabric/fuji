package io.github.sakurawald.fuji.core.event.inject.structure;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
public class EventGraph {

    @NotNull EventProducerInfoList producers = new EventProducerInfoList();
    @NotNull EventConsumerInfoList consumers = new EventConsumerInfoList();

    public @NotNull List<EventProducerInfo> ofEventProducerInfoList(@NotNull String eventTypeClassName) {
        return this.getProducers()
            .stream()
            .filter(it -> it.getEventTypeClassName().equals(eventTypeClassName))
            .toList();
    }

    private @NotNull List<EventConsumerInfo> ofEventConsumerInfoList(@NotNull String eventTypeClassName) {
        return this.getConsumers()
            .stream()
            .filter(it -> it.getEventTypeClassName().equals(eventTypeClassName))
            .toList();
    }
}
