package mod.fuji.core.auxiliary.minecraft;

import java.util.stream.Stream;
import mod.fuji.Fuji;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class RegistryHelper {

    public static @NotNull String getIdAsString(@NotNull ResourceLocation identifier) {
        return identifier.toString();
    }

    public static @NotNull String getIdAsString(@NotNull Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    public static @NotNull String getIdAsString(@NotNull ItemStack itemStack) {
        return getIdAsString(itemStack.getItem());
    }

    public static @NotNull String getIdAsString(@NotNull Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }

    public static @NotNull String getIdAsString(@NotNull BlockState blockState) {
        return getIdAsString(blockState.getBlock());
    }

    public static @NotNull String getIdAsString(@NotNull Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }

    public static @NotNull String getIdAsString(@NotNull Level world) {
        return getIdAsString(world.dimension());
    }

    public static @NotNull String getIdAsString(@NotNull ResourceKey<?> registryKey) {
        return registryKey.location().toString();
    }

    public static <T> @NotNull String getIdAsString(@NotNull Holder<T> registryEntry) {
        return registryEntry
            .unwrapKey()
            .map(RegistryHelper::getIdAsString)
            .orElse("[unregistered]");
    }

    /**
 * In older MC versions, the MessageType will not carry the registry bits.
 **/
    public static String getIdAsString(@NotNull ChatType.Bound parameters) {
        String messageTypeIdString;

        #if MC_VER <= MC_1_20_4
        MessageType messageTypeObj = parameters.type();
        messageTypeIdString = RegistryHelper.findRegistryKey(net.minecraft.registry.RegistryKeys.MESSAGE_TYPE, messageTypeObj)
            .map(RegistryHelper::getIdAsString)
            .orElseThrow(() -> new IllegalStateException("Failed to find the RegistryKey for MessageType %s".formatted(messageTypeObj)));
        #elif MC_VER > MC_1_20_4
        messageTypeIdString = parameters.chatType().getRegisteredName();
        #endif

        return messageTypeIdString;
    }

    public static RegistryAccess.Frozen getCombinedRegistryManager() {
        return ServerHelper
            .getServer()
            .registries()
            .compositeAccess();
    }

    public static HolderLookup.Provider getDefaultWrapperLookup() {
        return getCombinedRegistryManager();
    }

    /**
 * The Registry is one of the implementations of RegistryEntryLookup.
 **/
    public static <T> Registry<T> getRegistry(@NotNull ResourceKey<? extends Registry<? extends T>> registryKey) {
        return getCombinedRegistryManager()
            #if MC_VER <= MC_1_21
                .registryOrThrow(registryKey);
            #elif MC_VER > MC_1_21
                .lookupOrThrow(registryKey);
            #endif
    }

    /**
 * The RegistryEntryLookup is the interface for all types of registries.
 **/
    public static <T> HolderGetter<T> getRegistryEntryLookup(@NotNull ResourceKey<? extends Registry<? extends T>> registryKey) {
        return getCombinedRegistryManager()
            #if MC_VER <= MC_1_21
                .lookupOrThrow(registryKey);
            #elif MC_VER > MC_1_21
                .lookupOrThrow(registryKey);
            #endif
    }

    public static <T> ResourceKey<T> ofRegistryKey(@NotNull ResourceKey<? extends Registry<T>> registrySpecifier, @NotNull ResourceLocation identifier) {
        return ResourceKey.create(registrySpecifier, identifier);
    }

    public static <T> Optional<Holder<T>> getRegistryEntry(@NotNull ResourceKey<? extends Registry<T>> registrySpecifier, @NotNull ResourceLocation identifier) {
        Registry<T> registry = getRegistry(registrySpecifier);
        T object = getValue(registry, identifier);
        Holder<T> entry = registry.wrapAsHolder(object);
        return Optional.ofNullable(entry);
    }

    public static <T> T getValue(@NotNull Registry<T> registry, @NotNull ResourceKey<T> resourceKey) {
        #if MC_VER <= MC_1_21
        return registry.get(resourceKey);
        #elif MC_VER > MC_1_21
        return registry.getValue(resourceKey);
        #endif
    }

    public static <T> T getValue(@NotNull Registry<T> registry, @NotNull ResourceLocation identifier) {
        #if MC_VER <= MC_1_21
        return registry.get(identifier);
        #elif MC_VER > MC_1_21
        return registry.getValue(identifier);
        #endif
    }

    public static @NotNull ResourceLocation makeIdentifierOrThrow(@NotNull String identifier) {
        #if MC_VER <= MC_1_20_6
        return new Identifier(identifier);
        #elif MC_VER > MC_1_20_6
        return ResourceLocation.parse(identifier);
        #endif
    }

    public static Optional<ResourceLocation> makeIdentifier(@NotNull String identifier) {
        try {
            return Optional.of(makeIdentifierOrThrow(identifier));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static <T> Stream<Holder.Reference<T>> listElements(@NotNull Registry<T> registry) {
        #if MC_VER <= MC_1_21
        return registry.holders();
        #elif MC_VER > MC_1_21
        return registry.listElements();
        #endif
    }

    @SuppressWarnings("unused")
    public static <T> Optional<ResourceKey<T>> findRegistryKey(@NotNull ResourceKey<? extends Registry<? extends T>> registrySpecifier, @NotNull T registryValue) {
        return listElements(RegistryHelper.getRegistry(registrySpecifier))
            .filter(registryEntry -> registryValue.equals(registryEntry.value))
            .findFirst()
            .flatMap(registryEntry -> {
                Optional<ResourceKey<T>> key;
                try {
                    key = registryEntry.unwrapKey();
                } catch (Exception exceptionIfRegistryEntryHasNoRegistryKey) {
                    // NOTE: If the candidate didn't have a key, then we have nothing to return.
                    key = Optional.empty();
                }
                return key;
            });
    }

    public static void ensureIdentifierNamespaceIsFuji(@NotNull ResourceLocation identifier) {
        if (!identifier.getNamespace().equals(Fuji.MOD_ID)) {
            throw new IllegalArgumentException("The namespace of the identifier must be \"fuji\": " + identifier);
        }
    }

}
