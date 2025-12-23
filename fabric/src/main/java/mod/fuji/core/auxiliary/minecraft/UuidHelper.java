package mod.fuji.core.auxiliary.minecraft;

import mod.fuji.Fuji;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.RandomUtil;
import mod.fuji.core.structure.GlobalBlockPos;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class UuidHelper {

    private static final String FUJI_UUID_NBT_KEY = Fuji.MOD_ID + "$uuid";

    public static @NotNull String convertStringToUUID(@NotNull String string) {
        // NOTE: Convert to UUID, to ensure the string is valid filesystem path.
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        return UUID.nameUUIDFromBytes(bytes).toString();
    }

    public static Optional<String> getAttachedUuid(@Nullable CompoundTag root) {
        if (root == null) return Optional.empty();
        if (!root.contains(FUJI_UUID_NBT_KEY)) return Optional.empty();

        return NbtHelper.Primitives.getString(root, FUJI_UUID_NBT_KEY);
    }

    public static Optional<String> getAttachedUuid(@NotNull ItemStack itemStack) {
        @Nullable CompoundTag customDataNbt = ItemStackHelper.CustomData.getCustomDataNbt(itemStack);
        return UuidHelper.getAttachedUuid(customDataNbt);
    }

    public static @NotNull String getAttachedUuid(@NotNull Entity entity) {
        return entity.getStringUUID();
    }

    public static @NotNull String getAttachedUuid(@NotNull GlobalBlockPos globalBlockPos) {
        ServerLevel dimension = WorldHelper.getWorldOrThrow(globalBlockPos.getDimension());
        BlockPos blockPos = globalBlockPos.toBlockPos();
        return getAttachedUuid(dimension, blockPos);
    }

    public static @NotNull String getAttachedUuid(@NotNull Level world, @NotNull BlockPos blockPos) {
        // NOTE: Some global pos may face the hash collision.
        String string = toString(world, blockPos);
        return convertStringToUUID(string);
    }

    public static @NotNull String toString(@NotNull Level world, @NotNull BlockPos blockPos) {
        String dimensionString = RegistryHelper.getIdAsString(world);
        String blockPosString = blockPos.getX() + "#" + blockPos.getY() + "#" + blockPos.getZ();
        return dimensionString + "#" + blockPosString;
    }

    public static @NotNull String getOrSetAttachedUuid(@NotNull ItemStack itemStack) {
        CompoundTag nbt = ItemStackHelper.CustomData.getCustomDataNbt(itemStack);

        /* Set the attached UUID first if absent. */
        return getAttachedUuid(nbt)
            .orElseGet(() -> {
                CompoundTag newValue = attachRandomUuidToNbtCompoundIfAbsent(nbt);
                ItemStackHelper.CustomData.setCustomDataNbt(itemStack, newValue);

                return getAttachedUuid(newValue)
                    .orElseThrow(() -> {
                        LogUtil.error("Failed to set UUID for item stack: {}", itemStack);
                        return new IllegalArgumentException("Failed to set UUID for item stack: " + itemStack);
                    });
            });
    }

    private static @NotNull CompoundTag attachRandomUuidToNbtCompoundIfAbsent(@Nullable CompoundTag root) {
        /* Ensure the nbt is not null. */
        if (root == null) {
            root = new CompoundTag();
        }

        /* Attach a new UUID if not existed. */
        if (!root.contains(FUJI_UUID_NBT_KEY)) {
            String uuidString = RandomUtil.randomUUID();
            root.putString(FUJI_UUID_NBT_KEY, uuidString);
        }

        return root;
    }

    public static @NotNull UUID getNilUUID() {
        #if MC_VER < MC_1_21_11
        return net.minecraft.Util.NIL_UUID;
        #elif MC_VER >= MC_1_21_11
        return net.minecraft.util.Util.NIL_UUID;
        #endif
    }
}
