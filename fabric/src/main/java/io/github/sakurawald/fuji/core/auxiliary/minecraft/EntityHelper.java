package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

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
        return entity instanceof net.minecraft.entity.decoration.LeashKnotEntity;
        #elif MC_VER > MC_1_20_6
        return entity instanceof net.minecraft.entity.decoration.BlockAttachedEntity;
        #endif
    }

    public static boolean isLeashed(Entity entity) {
        #if MC_VER <= MC_1_20_6
        return (entity instanceof net.minecraft.entity.mob.MobEntity mobEntity) && mobEntity.isLeashed();
        #elif MC_VER > MC_1_20_6
        return (entity instanceof net.minecraft.entity.Leashable leashable) && leashable.isLeashed();
        #endif
    }

    public static boolean isVehicleEntity(Entity entity) {
        #if MC_VER <= MC_1_20_2
        return (entity instanceof net.minecraft.entity.vehicle.BoatEntity)
        || (entity instanceof net.minecraft.entity.vehicle.AbstractMinecartEntity);
        #elif MC_VER > MC_1_20_2
        return entity instanceof net.minecraft.entity.vehicle.VehicleEntity;
        #endif
    }

    public static boolean isPlayerEntity(Entity entity) {
        return entity instanceof PlayerEntity;
    }

}
