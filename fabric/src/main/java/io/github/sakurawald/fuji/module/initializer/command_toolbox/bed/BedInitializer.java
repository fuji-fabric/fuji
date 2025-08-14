package io.github.sakurawald.fuji.module.initializer.command_toolbox.bed;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class BedInitializer extends ModuleInitializer {

    @Document(id = 1751825169906L, value = "Teleport to the bed location.")
    @CommandNode("bed")
    private static int $bed(@CommandSource @CommandTarget ServerPlayerEntity player) {
        #if MC_VER < MC_1_21_5
        BlockPos respawnPosition = player.getSpawnPointPosition();
        RegistryKey<World> respawnDimension = player.getSpawnPointDimension();
        #elif MC_VER >= MC_1_21_5
        BlockPos respawnPosition = null;
        RegistryKey<World> respawnDimension = null;
        ServerPlayerEntity.Respawn respawn = player.getRespawn();
        if (respawn != null) {
            respawnPosition = respawn.comp_3684();
            respawnDimension = respawn.comp_3683();
        }
        #endif

        ServerWorld world = ServerHelper.getServer().getWorld(respawnDimension);
        if (respawnPosition == null || world == null) {
            TextHelper.sendTextByKey(player, "bed.not_found");
            return CommandHelper.Return.FAILURE;
        }

        new GlobalPos(world, respawnPosition.getX(), respawnPosition.getY(), respawnPosition.getZ(), player.getYaw(), player.getPitch())
            .teleport(player);
        TextHelper.sendTextByKey(player, "bed.success");
        return CommandHelper.Return.SUCCESS;
    }
}
