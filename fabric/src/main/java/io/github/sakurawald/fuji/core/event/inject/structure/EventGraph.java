package io.github.sakurawald.fuji.core.event.inject.structure;

import java.util.TreeMap;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
public class EventGraph {

    @NotNull TreeMap<String, EventProducerInfoList> producers = new TreeMap<>();
    @NotNull TreeMap<String, EventConsumerInfoList> consumers = new TreeMap<>();

}
