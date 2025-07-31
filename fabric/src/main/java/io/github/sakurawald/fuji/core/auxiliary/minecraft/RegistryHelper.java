package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.message.MessageType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class RegistryHelper {

    public static @NotNull String toString(@NotNull Item item) {
        return Registries.ITEM.getId(item).toString();
    }

    public static @NotNull String toString(@NotNull ItemStack itemStack) {
        return toString(itemStack.getItem());
    }

    public static @NotNull String toString(@NotNull Block block) {
        return Registries.BLOCK.getId(block).toString();
    }

    public static @NotNull String toString(@NotNull BlockState blockState) {
        return toString(blockState.getBlock());
    }

    public static @NotNull String toString(@NotNull Entity entity) {
        return Registries.ENTITY_TYPE.getId(entity.getType()).toString();
    }

    public static @NotNull String toString(@NotNull World world) {
        return toString(world.getRegistryKey());
    }

    public static @NotNull String toString(@NotNull RegistryKey<?> registryKey) {
        return registryKey.getValue().toString();
    }

    public static DynamicRegistryManager.Immutable getCombinedRegistryManager() {
        return ServerHelper
            .getServer()
            .getCombinedDynamicRegistries()
            .getCombinedRegistryManager();
    }

    public static <T> Registry<T> getRegistry(RegistryKey<? extends Registry<? extends T>> registryKey) {
        return getCombinedRegistryManager()
            #if MC_VER <= MC_1_21
                .get(registryKey);
            #elif MC_VER > MC_1_21
                .getOrThrow(registryKey);
            #endif
    }

    public static <T> RegistryEntryLookup<T> getRegistryWrapper(RegistryKey<? extends Registry<? extends T>> registryKey) {
        return getCombinedRegistryManager()
            #if MC_VER <= MC_1_21
                .getWrapperOrThrow(registryKey);
            #elif MC_VER > MC_1_21
                .getOrThrow(registryKey);
            #endif
    }

    public static <T> RegistryKey<T> getRegistryKey(@NotNull RegistryKey<? extends Registry<T>> keyOfRegistry, Identifier identifier) {
        return RegistryKey.of(keyOfRegistry, identifier);
    }

    public static <T> Optional<RegistryEntry<T>> getRegistryEntry(@NotNull RegistryKey<? extends Registry<T>> keyOfRegistry, Identifier identifier) {
        Registry<T> registry = getRegistry(keyOfRegistry);
        T t = registry.get(identifier);
        RegistryEntry<T> entry = registry.getEntry(t);
        return Optional.ofNullable(entry);
    }

    public static @Nullable ServerWorld getServerWorld(@Nullable String identifier) {
        if (identifier == null) return null;

        RegistryKey<World> key = getRegistryKey(RegistryKeys.WORLD, RegistryHelper.makeIdentifier(identifier));
        return ServerHelper
            .getServer()
            .getWorld(key);
    }

    public static @NotNull Item getItem(@NotNull String identifier) {
        // NOTE: For un-existed identifier, it will always return minecraft:air as the dummy item.
        Item item = Registries.ITEM.get(Identifier.tryParse(identifier));
        if (Items.AIR.equals(item)) {
            LogUtil.warn("Failed to find the item {} in registry, we will return BARRIER instead.", identifier);
            return Items.BARRIER;
        }
        return item;
    }

    public static RegistryWrapper.WrapperLookup getDefaultWrapperLookup() {
        return ServerHelper
            .getServer()
            .getRegistryManager();
    }

    public static Identifier makeIdentifier(String identifier) {
        #if MC_VER <= MC_1_20_6
            return new Identifier(identifier);
        #elif MC_VER > MC_1_20_6
            return Identifier.of(identifier);
        #endif
    }

    public static Optional<Identifier> tryMakeIdentifier(String identifier) {
        try {
            return Optional.of(makeIdentifier(identifier));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static <T> String getIdAsString(RegistryEntry<T> entry) {
        return entry
            .getKey()
            .map((registryKey) -> registryKey.getValue().toString())
            .orElse("[unregistered]");
    }

    public static <T> @Nullable String findRegistryKeyByRegistryValueInTheSpecifiedRegistry(RegistryKey<? extends Registry<? extends T>> registrySpecifier, Object theRegistryValue) {
        var ref = new Object() {
            String result;
        };

        RegistryHelper
            .getRegistry(registrySpecifier)
            .streamEntries()
            .forEach(candidate -> {
                Optional<RegistryKey<T>> candidateKey = candidate.getKey();
                // If the candidate didn't have a key, then we have nothing to return.
                if (candidateKey.isPresent()) {
                    if (theRegistryValue.equals(candidate.value)) {
                        ref.result = candidateKey.get().getValue().toString();
                    }
                }
            });

        // Return the identifier.
        return ref.result;
    }

    public static String getMessageTypeAsString(MessageType.Parameters parameters) {
        String messageTypeString;

        #if MC_VER <= MC_1_20_4
        MessageType messageTypeObj = parameters.type();
        messageTypeString = RegistryHelper.findRegistryKeyByRegistryValueInTheSpecifiedRegistry(RegistryKeys.MESSAGE_TYPE, messageTypeObj);
        #elif MC_VER > MC_1_20_4
            messageTypeString = parameters.type().getIdAsString();
        #endif

        return messageTypeString;
    }

    public static void ensureIdentifierNamespaceIfFuji(Identifier identifier) {
        if (!identifier.getNamespace().equals(Fuji.MOD_ID)) {
            throw new IllegalArgumentException("The namespace of identifier must be \"fuji\": " + identifier);
        }
    }

}
