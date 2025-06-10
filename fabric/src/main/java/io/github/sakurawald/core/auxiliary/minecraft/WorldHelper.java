package io.github.sakurawald.core.auxiliary.minecraft;

import io.github.sakurawald.core.service.random_teleport.RandomTeleporter;
import io.github.sakurawald.core.structure.TeleportSetup;
import lombok.experimental.UtilityClass;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

@UtilityClass
public class WorldHelper {

    public static void teleportToSafePositionNearOrigin(ServerWorld world, ServerPlayerEntity player) {
        RandomTeleporter.request(player,
            new TeleportSetup(
                RegistryHelper.ofString(world)
                ,0
                , 0
                , false
                , 0
                , 256
                ,0
                , 128
                ,32
        ), null);
    }

    public static int getTopY(Chunk chunk) {
        #if MC_VER == MC_1_21
            return chunk.getTopY();
        #elif MC_VER > MC_1_21
            return chunk.getTopYInclusive();
        #endif
    }

    public static int getTopY(World world) {
        #if MC_VER == MC_1_21
            return world.getTopY();
        #elif MC_VER > MC_1_21
            return world.getTopYInclusive();
        #endif
    }

}
