package io.github.sakurawald.fuji.core.event.inject.structure;

import io.github.sakurawald.fuji.core.annotation.ReflectiveAccess;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventConsumerInfo {

    @NotNull String eventTypeClassName;
    @NotNull String declaringClassName;
    @NotNull String declaringMethodName;

    int injectorPriority;

    @ReflectiveAccess
    int consumerPriority;

}
