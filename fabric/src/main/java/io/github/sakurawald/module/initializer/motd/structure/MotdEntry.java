package io.github.sakurawald.module.initializer.motd.structure;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
public class MotdEntry {

    @NotNull String text;
    @Nullable String icon;

}
