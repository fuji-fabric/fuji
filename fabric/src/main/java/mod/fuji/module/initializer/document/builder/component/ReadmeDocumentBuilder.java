package mod.fuji.module.initializer.document.builder.component;

import mod.fuji.module.initializer.document.builder.context.DocumentBuilderContext;
import org.jetbrains.annotations.NotNull;

public class ReadmeDocumentBuilder extends DocumentBuilder {
    @Override
    public void build(@NotNull DocumentBuilderContext documentBuilderContext) {
        documentBuilderContext
            .getDocumentBuilder()
            .append("""
                ---
                slug: /
                ---

                # README

                :::tip[Tip]
                ## How to Use This Document

                This document is divided into two main sections:

                1. **Core**
                   For the `core` part, please refer to the following resources:
                   - [Fuji Manual (PDF)](https://github.com/fuji-fabric/fuji/raw/dev/docs/release/fuji.pdf)
                   - [Core (HTML)](./001-Modules/002-core.md)

                2. **Modules**
                   For the `modules` part, please refer to the respective module chapter in this document.

                This document is also available in an interactive format, use `/fuji` command to play it.
                :::

                """);


    }
}
