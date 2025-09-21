package mod.fuji.module.initializer.command_toolbox.jump;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.core.structure.GlobalPos;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import java.util.Optional;


public class JumpInitializer extends ModuleInitializer {

    @CommandNode("jump")
    @CommandRequirement(level = 4)
    @Document(id = 1751825104871L, value = "Jump to the position looking at.")
    private static int $jump(@CommandSource @CommandTarget ServerPlayerEntity player
        , @Document(id = 1751825110041L, value = "The max distance to jump.") Optional<Integer> distance) {
        int $distance = distance.orElse(128);
        HitResult raycast = player.raycast($distance, 0, false);
        Vec3d hitPos = raycast.getPos();

        new GlobalPos(EntityHelper.getServerWorld(player), hitPos.x, hitPos.y, hitPos.z, player.getYaw(), player.getPitch())
            .teleport(player);
        return CommandHelper.Return.SUCCESS;
    }
}
