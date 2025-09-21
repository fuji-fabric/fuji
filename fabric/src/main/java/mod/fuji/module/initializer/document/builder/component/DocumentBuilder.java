package mod.fuji.module.initializer.document.builder.component;

import mod.fuji.module.initializer.document.builder.context.DocumentBuilderContext;
import org.jetbrains.annotations.NotNull;

public abstract class DocumentBuilder {

    public abstract void build(@NotNull DocumentBuilderContext documentBuilderContext);

}
