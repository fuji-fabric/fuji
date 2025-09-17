package io.github.sakurawald.fuji.module.initializer.document.builder;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class DocumentBuilderContext {
    @NotNull final String modulePathString;
    @NotNull final StringBuilder documentBuilder;
}
