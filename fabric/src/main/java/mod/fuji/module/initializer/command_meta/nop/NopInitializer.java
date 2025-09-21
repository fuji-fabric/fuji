package mod.fuji.module.initializer.command_meta.nop;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;

@Document(id = 1753517400537L, value = """
    This module provides the `/nop` command.
    This command `does nothing` and returns `success` directly.
    It can be used as a `dummy command`.
    """)
public class NopInitializer extends ModuleInitializer {

    @Document(id = 1754648842246L, value = "Do nothing, simply return SUCCESS as the command return value.")
    @CommandNode("nop")
    @CommandRequirement(level = 4)
    private static int $nop(@CommandSource ServerCommandSource source) {
        return CommandHelper.Return.SUCCESS;
    }

}
