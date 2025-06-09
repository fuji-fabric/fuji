package io.github.sakurawald.core.auxiliary.minecraft;

import io.github.sakurawald.Fuji;
import io.github.sakurawald.core.structure.SpatialBlock;
import lombok.experimental.UtilityClass;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
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

    public static @Nullable String getAttachedUuid(@Nullable NbtComponent nbtComponent) {
        if (nbtComponent == null) return null;

        NbtCompound root = nbtComponent.copyNbt();
        if (!root.contains(FUJI_UUID)) return null;

        return NbtHelper.getString(root, FUJI_UUID);
    }

    public static String getAttachedUuid(SpatialBlock spatialBlock) {
        return getAttachedUuid(spatialBlock.ofDimension(), spatialBlock.ofBlockPos());
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
        NbtComponent nbtComponent = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        if (getAttachedUuid(nbtComponent) == null) {
            nbtComponent = setGeneratedUuidIfAbsent(nbtComponent);
            itemStack.set(DataComponentTypes.CUSTOM_DATA, nbtComponent);
        }

        //noinspection DataFlowIssue
        return getAttachedUuid(nbtComponent);
    }

    private static @NotNull NbtComponent setGeneratedUuidIfAbsent(@Nullable NbtComponent nbtComponent) {
        /* extract nbt compound */
        NbtCompound root = nbtComponent == null ? new NbtCompound() : nbtComponent.copyNbt();

        /* put uuid if not exists */
        if (!root.contains(FUJI_UUID)) {
            root.putString(FUJI_UUID, String.valueOf(UUID.randomUUID()));
        }

        return NbtComponent.of(root);
    }
}
