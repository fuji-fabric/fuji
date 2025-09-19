package io.github.sakurawald.fuji.module.initializer.document.builder.component;

import io.github.sakurawald.fuji.module.initializer.document.builder.context.DocumentBuilderContext;
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

                :::tip
                ## How to Use This Document

                This document is divided into two main sections:

                1. **Core**
                   For the core functionality, please refer to the following resources:
                   - [Fuji Manual (PDF)](https://github.com/sakurawald/fuji/raw/dev/docs/release/fuji.pdf)
                   - [Core Documentation](./02-Modules/core.md)

                2. **Modules**
                   For additional modules, please consult the respective module chapters in this document.

                3. **Unlisted Features**
                   For any features not covered in this document, use the `/fuji` command to explore available options.
                :::

                """);


    }
}
