package io.github.sakurawald.core.auxiliary.minecraft;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

@UtilityClass
public class EntityHelper {

    public static void killEntity(Entity entity) {
        #if MC_VER == MC_1_21
            entity.kill();
        #elif MC_VER > MC_1_21
            entity.kill((ServerWorld) entity.getWorld());
        #endif
    }

    public static ServerWorld getServerWorld(Entity entity) {
        return (ServerWorld) entity.getWorld();
    }

    public static MinecraftServer getMinecraftServer(Entity entity) {
        return entity.getServer();
    }

}
