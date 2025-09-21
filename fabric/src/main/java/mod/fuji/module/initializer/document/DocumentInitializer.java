package mod.fuji.module.initializer.document;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.manager.impl.module.ModuleLoadDeterminer;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.document.builder.MarkdownDocumentBuilder;
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
