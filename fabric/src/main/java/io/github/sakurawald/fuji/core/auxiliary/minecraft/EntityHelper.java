package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

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

    @SuppressWarnings("unused")
    public static boolean isPlayerEntity(Entity entity) {
        return entity instanceof PlayerEntity;
    }

    public static void applyVelocity(@NotNull Entity entity, double x, double y, double z) {
        entity.addVelocity(x, y, z);
        EntityVelocityUpdateS2CPacket packet = new EntityVelocityUpdateS2CPacket(entity);
        PacketHelper.sendPacketToAll(packet);
    }
}
