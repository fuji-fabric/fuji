package io.github.sakurawald.fuji.core.event.inject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventProducerInfo {

    @NotNull String declaringClassName;
    @NotNull String declaringMethodName;

}
