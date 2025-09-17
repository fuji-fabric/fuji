package io.github.sakurawald.fuji.module.initializer.document.builder;

import org.jetbrains.annotations.NotNull;

public record DocumentBuilderContext(@NotNull String modulePathString, @NotNull StringBuilder documentBuilder) {

}
