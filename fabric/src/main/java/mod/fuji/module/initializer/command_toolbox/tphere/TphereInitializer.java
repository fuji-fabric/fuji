package mod.fuji.module.initializer.command_toolbox.tphere;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.PlayerCollection;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.structure.GlobalPos;
import mod.fuji.module.initializer.ModuleInitializer;
import java.util.Collection;
import net.minecraft.server.level.ServerPlayer;

@Document(id = 1751825147630L, value = """
    This is a convenient command, to teleport `others` to `you`.
    Similar to `/tp @a <player>`.

    For example:
    1. `/tphere Steve` to teleport `Steve` to `you`.
    2. `/tphere @a` to teleport `all online players` to `you`.
    """)

@CommandNode("tphere")
@CommandRequirement(level = 4)
public class TphereInitializer extends ModuleInitializer {

    @CommandNode
    private static int $tphere(@CommandSource ServerPlayer player, PlayerCollection targets) {
        Collection<ServerPlayer> $targets = targets.getValue();
        GlobalPos globalPos = GlobalPos.of(player);

        $targets.forEach(globalPos::teleport);
        return CommandHelper.Return.SUCCESS;
    }

}
