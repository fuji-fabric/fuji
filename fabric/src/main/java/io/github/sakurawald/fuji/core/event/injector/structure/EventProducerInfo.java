package io.github.sakurawald.fuji.core.event.injector.structure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventProducerInfo {

    @NotNull String eventTypeClassName;
    @NotNull String declaringClassName;
    @Nullable String declaringMethodName;
    int injectorPriority;

}
