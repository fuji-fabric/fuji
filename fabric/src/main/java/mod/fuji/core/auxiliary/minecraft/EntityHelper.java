package mod.fuji.core.auxiliary.minecraft;

import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

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

    public static void deleteEntity(@NotNull Entity entity) {
        entity.discard();
    }

    public static void dropItem(@NotNull LivingEntity entity, @NotNull ItemStack itemStack) {
        #if MC_VER < MC_26_3
        entity.drop(itemStack, false);
        #elif MC_VER >= MC_26_3
        entity.drop(itemStack, false, net.minecraft.util.Prediction.SERVER_ONLY);
        #endif
    }

    @SuppressWarnings("SameParameterValue")
    public static void setInvulnerable(@NotNull Entity entity, boolean value) {
        #if MC_VER < MC_26_3
        entity.setInvulnerable(value);
        #elif MC_VER >= MC_26_3
        entity.setPermanentlyInvulnerable(value);
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

        public static void markVelocityChanged(@NonNull Entity entity) {
            #if MC_VER < MC_26_3
            entity.hurtMarked = true;
            #elif MC_VER >= MC_26_3
            entity.syncVelocity = true;
            #endif
        }
    }

    public static class Predicates {

        public static boolean isLeashed(@NotNull Entity entity) {
            #if MC_VER <= MC_1_20_6
            return (entity instanceof net.minecraft.world.entity.monster.Monster mobEntity) && mobEntity.isLeashed();
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

    public static class Loader {

        #if MC_VER >= MC_1_21_9
        // FIXME Use the loadNbt function where possible
        private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
        public static void loadNbt(@NotNull Entity entity, @NotNull CompoundTag tag) {
            try (var reporter = new net.minecraft.util.ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER)) {
                var tagValueInput = net.minecraft.world.level.storage.TagValueInput.create(reporter, ServerHelper.getServer().registryAccess(), tag);
                entity.load(tagValueInput);
            }
        }
        #endif
    }

    public static class SignBlock {

        /* Define the possible sides of a sign block entity. (Only front side and back side) */
        #if MC_VER < MC_26_3
        public static boolean toFacingFront(boolean signTextSpecifier) {
            return signTextSpecifier;
        }
        #elif MC_VER >= MC_26_3
        public static boolean toFacingFront(net.minecraft.world.level.block.entity.SignTextSlot signTextSpecifier) {
            return switch (signTextSpecifier) {
                case net.minecraft.world.level.block.entity.SignTextSlot.FRONT -> true;
                case net.minecraft.world.level.block.entity.SignTextSlot.BACK -> false;
            };
        }

        private static @NotNull net.minecraft.world.level.block.entity.SignTextSlot toFacingSlot(boolean signTextSpecifier) {
            if (signTextSpecifier) {
                return net.minecraft.world.level.block.entity.SignTextSlot.FRONT;
            }
            return net.minecraft.world.level.block.entity.SignTextSlot.BACK;
        }
        #endif

        public static boolean isFacingFront(@NotNull ServerPlayer player, @NotNull SignBlockEntity signBlockEntity) {
            #if MC_VER < MC_26_3
            return signBlockEntity.isFacingFrontText(player);
            #elif MC_VER >= MC_26_3
            var facingSlot = signBlockEntity.getSlotPlayerIsFacing(player);
            return toFacingFront(facingSlot);
            #endif
        }

        /* Define operators for a sign block entity. */
        public static @NotNull SignText getSignText(@NotNull SignBlockEntity signBlockEntity, boolean isFront) {
            #if MC_VER < MC_26_3
            return signBlockEntity.getText(isFront);
            #elif MC_VER >= MC_26_3
            var facingSlot = toFacingSlot(isFront);
            return signBlockEntity.getText(facingSlot);
            #endif
        }

        public static @NotNull SignText getFacingSignText(@NotNull ServerPlayer player, @NotNull SignBlockEntity signBlockEntity) {
            boolean isFacingFront = isFacingFront(player, signBlockEntity);
            return getSignText(signBlockEntity, isFacingFront);
        }

        public static @NotNull Component[] getTextArray(@NotNull SignText signText) {
            #if MC_VER < MC_26_3
            return signText.getMessages(false);
            #elif MC_VER >= MC_26_3
            return signText.getMessages(false).toArray(Component[]::new);
            #endif
        }

        public static @NotNull Stream<Component> getTextStream(@NotNull SignText signText) {
            return Arrays.stream(getTextArray(signText));
        }

        public static void updateSignText(@NotNull SignBlockEntity signBlockEntity, @NotNull UnaryOperator<SignText> mapper, boolean isFront) {
            #if MC_VER < MC_26_3
            signBlockEntity.updateText(mapper, isFront);
            #elif MC_VER >= MC_26_3
            var signTextSlot = toFacingSlot(isFront);
            signBlockEntity.updateText(mapper, signTextSlot);
            #endif
        }

        public static
        #if MC_VER < MC_26_3
        Component[]
        #elif MC_VER >= MC_26_3
        List<Component>
        #endif
        coerceLineTexts(@NotNull Component[] components) {
            #if MC_VER < MC_26_3
            return components;
            #elif MC_VER >= MC_26_3
            return List.of(components);
            #endif
        }
    }

}
