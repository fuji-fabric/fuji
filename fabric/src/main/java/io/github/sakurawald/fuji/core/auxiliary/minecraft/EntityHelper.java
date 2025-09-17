package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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

    public static boolean isLeashed(Entity entity) {
        #if MC_VER <= MC_1_20_6
        return (entity instanceof net.minecraft.entity.mob.MobEntity mobEntity) && mobEntity.isLeashed();
        #elif MC_VER > MC_1_20_6
        return (entity instanceof net.minecraft.entity.Leashable leashable) && leashable.isLeashed();
        #endif
    }

    @SuppressWarnings("unused")
    public static boolean isPlayerEntity(Entity entity) {
        return entity instanceof PlayerEntity;
    }

    public static void addVelocity(@NotNull Entity entity, double x, double y, double z) {
        entity.addVelocity(x, y, z);
        updateVelocity(entity);
    }

    public static void setVelocity(@NotNull Entity entity, double x, double y, double z) {
        entity.setVelocity(x, y, z);
        updateVelocity(entity);
    }

    public static void updateVelocity(@NotNull Entity entity) {
        EntityVelocityUpdateS2CPacket packet = new EntityVelocityUpdateS2CPacket(entity);
        PacketHelper.sendPacketToAll(packet);
    }

    public static @NotNull String toTranslatableKey(@NotNull Entity entity) {
        String translatableKey;

        if (entity instanceof ItemEntity itemEntity) {
            translatableKey = itemEntity.getStack().getItem().getTranslationKey();
        } else {
            translatableKey = entity.getType().getTranslationKey();
        }
        return translatableKey;
    }

    public static boolean hasCustomName(@NotNull Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            ItemStack itemStack = itemEntity.getStack();
            return ItemStackHelper.CustomName.hasCustomName(itemStack);
        } else {
            return entity.hasCustomName();
        }
    }

    public static boolean isLivingEntity(@NotNull Entity entity) {
        return entity.isLiving();
    }

    public static boolean hasVehicle(@NotNull Entity entity) {
        return entity.hasVehicle();
    }

    public static boolean hasPassengers(@NotNull Entity entity) {
        return entity.hasPassengers();
    }

    public static boolean isItemEntity(@NotNull Entity entity) {
        return entity instanceof ItemEntity;
    }

    public static boolean isGlowing(@NotNull Entity entity) {
        return entity.isGlowing();
    }

    public static int getEntityEffectiveCount(@NotNull Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            return itemEntity.getStack().getCount();
        } else {
            return 1;
        }
    }

    public static byte withFlagValue(int base, int flag, boolean value) {
        return (byte) (value ? base | flag : base & ~flag);
    }

    public static int getAge(Entity entity) {
        return entity.age;
    }
}
