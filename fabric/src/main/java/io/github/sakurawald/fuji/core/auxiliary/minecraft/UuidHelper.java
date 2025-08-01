package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.auxiliary.RandomUtil;
import io.github.sakurawald.fuji.core.structure.GlobalBlockPos;
import java.nio.charset.StandardCharsets;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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

    public static @Nullable String getAttachedUuid(@Nullable NbtCompound root) {
        if (root == null) return null;
        if (!root.contains(FUJI_UUID_NBT_KEY)) return null;

        return NbtHelper.Primitives.getString(root, FUJI_UUID_NBT_KEY);
    }

    public static String getAttachedUuid(@NotNull GlobalBlockPos globalBlockPos) {
        ServerWorld dimension = ServerHelper.getWorldOrThrow(globalBlockPos.getDimension());
        BlockPos blockPos = globalBlockPos.toBlockPos();
        return getAttachedUuid(dimension, blockPos);
    }

    public static @NotNull String getAttachedUuid(@NotNull World world, @NotNull BlockPos blockPos) {
        // NOTE: Some global pos may face the hash collision.
        String string = toString(world, blockPos);
        return convertStringToUUID(string);
    }

    public static @NotNull String toString(@NotNull World world, @NotNull BlockPos blockPos) {
        String dimensionString = RegistryHelper.toIdString(world);
        String blockPosString = blockPos.getX() + "#" + blockPos.getY() + "#" + blockPos.getZ();
        return dimensionString + "#" + blockPosString;
    }

    public static @NotNull String getOrSetAttachedUuid(@NotNull ItemStack itemStack) {
        NbtCompound nbt = ItemStackHelper.Nbt.getNbt(itemStack);

        /* Set the attached UUID first if absent. */
        if (getAttachedUuid(nbt) == null) {
            nbt = attachRandomUuidToNbtCompoundIfAbsent(nbt);
            ItemStackHelper.Nbt.setNbt(itemStack, nbt);
        }

        /* Get the attached UUID. */
        return getAttachedUuid(nbt);
    }

    private static @NotNull NbtCompound attachRandomUuidToNbtCompoundIfAbsent(@Nullable NbtCompound root) {
        /* Ensure the nbt is not null. */
        if (root == null) {
            root = new NbtCompound();
        }

        /* Attach a new UUID if not existed. */
        if (!root.contains(FUJI_UUID_NBT_KEY)) {
            String uuidString = RandomUtil.randomUUID();
            root.putString(FUJI_UUID_NBT_KEY, uuidString);
        }

        return root;
    }
}
