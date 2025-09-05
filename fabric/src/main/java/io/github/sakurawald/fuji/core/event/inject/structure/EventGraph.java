package io.github.sakurawald.fuji.core.event.inject.structure;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
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

    private Optional<EventProducerInfo> resolveProducer(@NotNull String mixinClassName) {
        return this.getProducers()
            .stream()
            .filter(it -> it.getDeclaringClassName().equals(mixinClassName))
            .findFirst();
    }

    @SuppressWarnings("CodeBlock2Expr")
    public @NotNull List<EventConsumerInfo> resolveConsumers(@NotNull String mixinClassName) {
        return resolveProducer(mixinClassName)
            .map(eventProducerInfo -> {
                return this.getConsumers()
                    .stream()
                    .filter(eventConsumerInfo -> matches(eventProducerInfo, eventConsumerInfo));
            })
            .orElseGet(() -> {
                LogUtil.error("""
                    [Missing Event Producer]
                    There is no EventConsumerInfo found for mixin class {}.

                    ◉ Solution: If you see this, you should create an issue in https://github.com/sakurawald/fuji/issues
                    """);
                return Stream.empty();
            })
            .toList();
    }
}
