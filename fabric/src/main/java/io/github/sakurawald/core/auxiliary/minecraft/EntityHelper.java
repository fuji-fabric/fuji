package io.github.sakurawald.core.auxiliary.minecraft;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
#if MC_VER <= MC_1_20_6
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.mob.MobEntity;
#elif MC_VER > MC_1_20_6
import net.minecraft.entity.Leashable;
import net.minecraft.entity.decoration.BlockAttachedEntity;
#endif
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

#if MC_VER <= MC_1_20_2
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
#elif MC_VER > MC_1_20_2
import net.minecraft.entity.vehicle.VehicleEntity;
#endif

@UtilityClass
public class EntityHelper {

    public static void killEntity(Entity entity) {
        #if MC_VER <= MC_1_21
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

    public static boolean isBlockAttachedEntity(Entity entity) {
        #if MC_VER <= MC_1_20_6
            return entity instanceof LeashKnotEntity;
        #elif MC_VER > MC_1_20_6
            return entity instanceof BlockAttachedEntity;
        #endif
    }

    public static boolean isLeashed(Entity entity) {
        #if MC_VER <= MC_1_20_6
            return (entity instanceof MobEntity mobEntity) && mobEntity.isLeashed();
        #elif MC_VER > MC_1_20_6
            return (entity instanceof Leashable leashable) && leashable.isLeashed();
        #endif
    }

    public static boolean isVehicleEntity(Entity entity) {
        #if MC_VER <= MC_1_20_2
            return (entity instanceof BoatEntity)
                || (entity instanceof AbstractMinecartEntity);
        #elif MC_VER > MC_1_20_2
            return entity instanceof VehicleEntity;
        #endif
    }

    public static boolean isPlayer(Entity entity) {
        return entity instanceof PlayerEntity;
    }

}
