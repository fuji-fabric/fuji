package io.github.sakurawald.fuji.module.initializer.document;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.document.structure.DocumentBuilderDriver;

@Document(id = 1758088953109L, value = """
    This module provides the tools to `generate mod document`.
    <green>NOTE: This module is typically used by developers, to generate the user manual.
    """)
public class DocumentInitializer extends ModuleInitializer {

    @CommandNode("document build-all")
    @CommandRequirement(level = 4)
    private static int $generateAll() {
        DocumentBuilderDriver.buildAll();
        return CommandHelper.Return.SUCCESS;
    }

}
