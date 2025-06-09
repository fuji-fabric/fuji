package io.github.sakurawald.core.auxiliary.minecraft;

import lombok.experimental.UtilityClass;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class RegistryHelper {

    public static @NotNull String ofString(@NotNull Item item) {
        return Registries.ITEM.getId(item).toString();
    }

    public static @NotNull String ofString(@NotNull ItemStack itemStack) {
        return ofString(itemStack.getItem());
    }

    public static @NotNull String ofString(Block block) {
        return Registries.BLOCK.getId(block).toString();
    }

    public static @NotNull String ofString(@NotNull BlockState blockState) {
        return ofString(blockState.getBlock());
    }

    public static @NotNull String ofString(@NotNull Entity entity) {
        return Registries.ENTITY_TYPE.getId(entity.getType()).toString();
    }

    public static @NotNull String ofString(@NotNull World world) {
        return world.getRegistryKey().getValue().toString();
    }

    public static <T> Registry<T> ofRegistry(RegistryKey<? extends Registry<? extends T>> registryKey) {
        return ServerHelper.getServer()
            .getCombinedDynamicRegistries()
            .getCombinedRegistryManager()
            .getOrThrow(registryKey);
    }

    public static <T> RegistryKey<T> ofRegistryKey(@NotNull RegistryKey<? extends Registry<T>> keyOfRegistry, Identifier identifier) {
        return RegistryKey.of(keyOfRegistry, identifier);
    }

    public static @Nullable ServerWorld ofServerWorld(String identifier) {
        RegistryKey<World> key = ofRegistryKey(RegistryKeys.WORLD, Identifier.of(identifier));
        // get the world instance from the server.
        return ServerHelper.getServer().getWorld(key);
    }

    public static @NotNull Item ofItem(@NotNull String identifier) {
        return Registries.ITEM.get(Identifier.tryParse(identifier));
    }

    public static RegistryWrapper.WrapperLookup getDefaultWrapperLookup() {
        return ServerHelper.getServer()
            .getRegistryManager();
    }
}
