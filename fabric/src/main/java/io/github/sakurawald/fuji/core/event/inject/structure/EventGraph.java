package io.github.sakurawald.fuji.core.event.inject.structure;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
public class EventGraph {

    @NotNull Map<String, EventProducerInfoList> producers = new HashMap<>();
    @NotNull Map<String, EventConsumerInfoList> consumers = new HashMap<>();

}
