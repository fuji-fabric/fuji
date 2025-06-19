package io.github.sakurawald.core.auxiliary.minecraft;

import io.github.sakurawald.Fuji;
import io.github.sakurawald.core.structure.GlobalBlockPos;
import lombok.experimental.UtilityClass;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@UtilityClass
public class UuidHelper {

    private static final String FUJI_UUID = Fuji.MOD_ID + "$uuid";

    public static @Nullable String getAttachedUuid(@Nullable NbtCompound root) {
        if (root == null) return null;

        if (!root.contains(FUJI_UUID)) return null;
        return NbtHelper.getString(root, FUJI_UUID);
    }

    public static String getAttachedUuid(GlobalBlockPos globalBlockPos) {
        return getAttachedUuid(globalBlockPos.ofDimension(), globalBlockPos.ofBlockPos());
    }

    public static String getAttachedUuid(World world, BlockPos blockPos) {
        byte[] bytes = toUuid(world, blockPos).getBytes();
        return UUID.nameUUIDFromBytes(bytes).toString();
    }

    public static String toUuid(World world, BlockPos blockPos) {
        String dimension = RegistryHelper.ofString(world);
        String pos = blockPos.getX() + "#" + blockPos.getY() + "#" + blockPos.getZ();
        return dimension + "#" + pos;
    }

    public static @NotNull String getOrSetAttachedUuid(ItemStack itemStack) {
        NbtCompound nbt = NbtHelper.getNbt(itemStack);
        if (getAttachedUuid(nbt) == null) {
            nbt = setGeneratedUuidIfAbsent(nbt);
            NbtHelper.setNbt(itemStack, nbt);
        }

        //noinspection DataFlowIssue
        return getAttachedUuid(nbt);
    }

    private static @NotNull NbtCompound setGeneratedUuidIfAbsent(@Nullable NbtCompound root) {
        /* extract nbt compound */
        if (root == null) {
            root = new NbtCompound();
        }

        /* put uuid if not exists */
        if (!root.contains(FUJI_UUID)) {
            root.putString(FUJI_UUID, String.valueOf(UUID.randomUUID()));
        }

        return root;
    }
}
