package io.github.sakurawald.fuji.module.initializer.document;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleLoadDeterminer;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.document.builder.MarkdownDocumentBuilder;
import net.minecraft.server.command.ServerCommandSource;

@Document(id = 1758088953109L, value = """
    This module provides the tools to `generate mod document`.
    <green>NOTE: This module is typically used by developers, to generate the user manual.
    """)
public class DocumentInitializer extends ModuleInitializer {

    @CommandNode("document build")
    @CommandRequirement(level = 4)
    private static int $build(@CommandSource ServerCommandSource source) {
        MarkdownDocumentBuilder.buildAll();
        TextHelper.sendTextByKey(source, "document.build", MarkdownDocumentBuilder.DOCUMENT_BUILD_DIR);

        int enabledModules = ModuleLoadDeterminer.getEnabledModulePaths().size();
        int declaredModules = ModuleLoadDeterminer.getDeclaredModulePaths().size();
        TextHelper.sendTextByKey(source, "document.modules", enabledModules, declaredModules);
        return CommandHelper.Return.SUCCESS;
    }

}
