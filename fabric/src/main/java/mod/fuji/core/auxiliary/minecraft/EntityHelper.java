package mod.fuji.core.auxiliary.minecraft;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class EntityHelper {

    public static void killEntity(@NotNull Entity entity) {
        #if MC_VER <= MC_1_21
        entity.kill();
        #elif MC_VER > MC_1_21
        entity.kill(EntityHelper.getServerWorld(entity));
        #endif
    }

    public static @NotNull ServerLevel getServerWorld(@NotNull Entity entity) {
        #if MC_VER < MC_1_21_9
        return (ServerLevel) entity.level();
        #elif MC_VER >= MC_1_21_9
        return (ServerLevel) entity.level();
        #endif
    }

    public static @NotNull String toTranslatableKey(@NotNull Entity entity) {
        String translatableKey;

        if (entity instanceof ItemEntity itemEntity) {
            translatableKey = itemEntity.getItem().getItem().getDescriptionId();
        } else {
            translatableKey = entity.getType().getDescriptionId();
        }
        return translatableKey;
    }

    public static int getEntityEffectiveCount(@NotNull Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            return itemEntity.getItem().getCount();
        } else {
            return 1;
        }
    }

    public static byte withFlagValue(int base, int flag, boolean value) {
        return (byte) (value ? base | flag : base & ~flag);
    }

    public static int getAge(@NotNull Entity entity) {
        return entity.tickCount;
    }

    public static @NotNull Vec3 getPos(@NotNull Entity entity) {
        return entity.position;
    }

    public static void moveEntity(@NotNull Entity entity, double x, double y, double z, float yRot, float xRot) {
        #if MC_VER <= MC_1_21_4
        entity.moveTo(x, y, z, yRot, xRot);
        #elif MC_VER > MC_1_21_4
        entity.snapTo(x, y, z, yRot, xRot);
        #endif
    }

    public static void rideEntity(@NotNull Entity passengerEntity, @NotNull Entity vehicleEntity) {
        #if MC_VER < MC_1_21_9
        passengerEntity.startRiding(vehicleEntity, true);
        #elif MC_VER >= MC_1_21_9
        passengerEntity.startRiding(vehicleEntity, true, false);
        #endif
    }

    public static class Physics {

        public static void addVelocity(@NotNull Entity entity, double x, double y, double z) {
            entity.push(x, y, z);
            updateVelocity(entity);
        }

        public static void setVelocity(@NotNull Entity entity, double x, double y, double z) {
            entity.setDeltaMovement(x, y, z);
            updateVelocity(entity);
        }

        public static void updateVelocity(@NotNull Entity entity) {
            ClientboundSetEntityMotionPacket packet = new ClientboundSetEntityMotionPacket(entity);
            PacketHelper.sendPacketToAll(packet);
        }
    }

    public static class Predicates {

        public static boolean isLeashed(@NotNull Entity entity) {
            #if MC_VER <= MC_1_20_6
            return (entity instanceof net.minecraft.entity.mob.MobEntity mobEntity) && mobEntity.isLeashed();
            #elif MC_VER > MC_1_20_6
            return (entity instanceof net.minecraft.world.entity.Leashable leashable) && leashable.isLeashed();
            #endif
        }

        public static boolean isLivingEntity(@NotNull Entity entity) {
            return entity.showVehicleHealth();
        }

        public static boolean hasVehicle(@NotNull Entity entity) {
            return entity.isPassenger();
        }

        public static boolean hasPassengers(@NotNull Entity entity) {
            return entity.isVehicle();
        }

        public static boolean isItemEntity(@NotNull Entity entity) {
            return entity instanceof ItemEntity;
        }

        public static boolean isGlowing(@NotNull Entity entity) {
            return entity.isCurrentlyGlowing();
        }

        public static boolean hasCustomName(@NotNull Entity entity) {
            if (entity instanceof ItemEntity itemEntity) {
                ItemStack itemStack = itemEntity.getItem();
                return ItemStackHelper.CustomName.hasCustomName(itemStack);
            } else {
                return entity.hasCustomName();
            }
        }
    }
}
