package io.github.sakurawald.module.initializer.command_toolbox.bed;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.annotation.CommandTarget;
import io.github.sakurawald.core.structure.SpatialPose;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class BedInitializer extends ModuleInitializer {

    @CommandNode("bed")
    @Document("Teleport to the bed location.")
    private static int $bed(@CommandSource @CommandTarget ServerPlayerEntity player) {
        #if MC_VER < MC_1_21_5
        BlockPos respawnPosition = player.getSpawnPointPosition();
        RegistryKey<World> respawnDimension = player.getSpawnPointDimension();
        #elif MC_VER >= MC_1_21_5
        ServerPlayerEntity.Respawn respawn = player.getRespawn();
        BlockPos respawnPosition = respawn.comp_3684();
        RegistryKey<World> respawnDimension = respawn.comp_3683();
        #endif

        ServerWorld world = ServerHelper.getServer().getWorld(respawnDimension);
        if (respawnPosition == null || world == null) {
            TextHelper.sendMessageByKey(player, "bed.not_found");
            return CommandHelper.Return.FAIL;
        }

        new SpatialPose(world, respawnPosition.getX(), respawnPosition.getY(), respawnPosition.getZ(), player.getYaw(), player.getPitch())
            .teleport(player);
        TextHelper.sendMessageByKey(player, "bed.success");
        return CommandHelper.Return.SUCCESS;
    }
}
