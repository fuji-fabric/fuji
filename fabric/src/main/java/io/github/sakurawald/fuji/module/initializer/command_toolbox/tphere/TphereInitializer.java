package io.github.sakurawald.fuji.module.initializer.command_toolbox.tphere;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.PlayerCollection;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import java.util.Collection;
import net.minecraft.server.network.ServerPlayerEntity;

@Document("""
    This is a convenient command, to teleport `others` to `you`.

    For example:
    1. `/tphere Steve` to teleport `Steve` to `you`.
    2. `/tphere @a` to teleport `all online players` to `you`.
    """)

@CommandNode("tphere")
@CommandRequirement(level = 4)
public class TphereInitializer extends ModuleInitializer {

    @CommandNode
    private static int $tphere(@CommandSource ServerPlayerEntity player, PlayerCollection targets) {
        Collection<ServerPlayerEntity> $targets = targets.getValue();
        GlobalPos globalPos = GlobalPos.of(player);

        $targets.forEach(globalPos::teleport);
        return CommandHelper.Return.SUCCESS;
    }

}
