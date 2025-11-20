package mod.fuji.module.initializer.command_toolbox.top;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.auxiliary.minecraft.WorldHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.structure.GlobalPos;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;


public class TopInitializer extends ModuleInitializer {

    @Document(id = 1751825123433L, value = "Teleport to the top of your current position.")
    @CommandNode("top")
    private static int $top(@CommandSource @CommandTarget ServerPlayer player) {
        ServerLevel serverWorld = PlayerHelper.getServerWorld(player);
        BlockPos blockPos = player.blockPosition();
        GlobalPos globalPos = WorldHelper.findSafeTopY(serverWorld, blockPos);
        globalPos.teleport(player);

        TextHelper.sendTextByKey(player, "top");
        return CommandHelper.Return.SUCCESS;
    }

}
