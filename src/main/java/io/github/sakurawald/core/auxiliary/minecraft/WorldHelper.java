package io.github.sakurawald.core.auxiliary.minecraft;

import io.github.sakurawald.core.service.random_teleport.RandomTeleporter;
import io.github.sakurawald.core.structure.TeleportSetup;
import lombok.experimental.UtilityClass;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

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
                , 512
                ,0
                , 128
                ,32
        ), null);
    }

}
