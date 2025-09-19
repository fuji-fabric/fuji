package io.github.sakurawald.fuji.module.initializer.document.builder.component;

import io.github.sakurawald.fuji.module.initializer.document.builder.context.DocumentBuilderContext;
import org.jetbrains.annotations.NotNull;

public abstract class DocumentBuilder {

    public abstract void build(@NotNull DocumentBuilderContext documentBuilderContext);

}
