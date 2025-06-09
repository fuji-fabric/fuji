package io.github.sakurawald.module.initializer.command_toolbox.jump;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.annotation.CommandTarget;
import io.github.sakurawald.core.structure.SpatialPose;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import java.util.Optional;


public class JumpInitializer extends ModuleInitializer {

    @CommandNode("jump")
    @CommandRequirement(level = 4)
    @Document("Jump to the position looking at.")
    private static int jump(@CommandSource @CommandTarget ServerPlayerEntity player
        , @Document("The max distance to jump.") Optional<Integer> distance) {
        int $distance = distance.orElse(128);
        HitResult raycast = player.raycast($distance, 0, false);
        Vec3d hitPos = raycast.getPos();

        new SpatialPose(player.getServerWorld(), hitPos.x, hitPos.y, hitPos.z, player.getYaw(), player.getPitch())
            .teleport(player);
        return CommandHelper.Return.SUCCESS;
    }
}
