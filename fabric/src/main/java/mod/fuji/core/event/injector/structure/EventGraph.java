package mod.fuji.core.event.injector.structure;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
public class EventGraph {

    @NotNull EventProducerInfoList producers = new EventProducerInfoList();
    @NotNull EventConsumerInfoList consumers = new EventConsumerInfoList();

    private boolean matches(@NotNull EventProducerInfo eventProducerInfo, @NotNull EventConsumerInfo eventConsumerInfo) {
        return eventProducerInfo.getEventTypeClassName().equals(eventConsumerInfo.getEventTypeClassName())
            && eventProducerInfo.getInjectorPriority() == eventConsumerInfo.getInjectorPriority();
    }

    private @NotNull List<EventProducerInfo> resolveProducers(@NotNull String mixinClassName) {
        return this.getProducers()
            .stream()
            .filter(it -> it.getDeclaringClassName().equals(mixinClassName))
            .toList();
    }

    @SuppressWarnings("CodeBlock2Expr")
    public @NotNull List<EventConsumerInfo> resolveConsumers(@NotNull String mixinClassName) {
        return this.resolveProducers(mixinClassName)
            .stream()
            .flatMap(eventProducerInfo -> {
                return this.getConsumers()
                    .stream()
                    .filter(eventConsumerInfo -> matches(eventProducerInfo, eventConsumerInfo));
            })
            .toList();
    }
}
