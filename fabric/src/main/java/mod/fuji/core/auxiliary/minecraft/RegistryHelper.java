package mod.fuji.core.auxiliary.minecraft;

import mod.fuji.Fuji;
import mod.fuji.core.document.annotation.ForDeveloper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.message.MessageType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class RegistryHelper {

    public static @NotNull String getIdAsString(@NotNull Identifier identifier) {
        return identifier.toString();
    }

    public static @NotNull String getIdAsString(@NotNull Item item) {
        return Registries.ITEM.getId(item).toString();
    }

    public static @NotNull String getIdAsString(@NotNull ItemStack itemStack) {
        return getIdAsString(itemStack.getItem());
    }

    public static @NotNull String getIdAsString(@NotNull Block block) {
        return Registries.BLOCK.getId(block).toString();
    }

    public static @NotNull String getIdAsString(@NotNull BlockState blockState) {
        return getIdAsString(blockState.getBlock());
    }

    public static @NotNull String getIdAsString(@NotNull Entity entity) {
        return Registries.ENTITY_TYPE.getId(entity.getType()).toString();
    }

    public static @NotNull String getIdAsString(@NotNull World world) {
        return getIdAsString(world.getRegistryKey());
    }

    public static @NotNull String getIdAsString(@NotNull RegistryKey<?> registryKey) {
        return registryKey.getValue().toString();
    }

    public static <T> @NotNull String getIdAsString(@NotNull RegistryEntry<T> registryEntry) {
        return registryEntry
            .getKey()
            .map(RegistryHelper::getIdAsString)
            .orElse("[unregistered]");
    }

    /**
 * In older MC versions, the MessageType will not carry the registry bits.
 **/
    public static String getIdAsString(@NotNull MessageType.Parameters parameters) {
        String messageTypeIdString;

        #if MC_VER <= MC_1_20_4
        MessageType messageTypeObj = parameters.type();
        messageTypeIdString = RegistryHelper.findRegistryKey(net.minecraft.registry.RegistryKeys.MESSAGE_TYPE, messageTypeObj)
            .map(RegistryHelper::getIdAsString)
            .orElseThrow(() -> new IllegalStateException("Failed to find the RegistryKey for MessageType %s".formatted(messageTypeObj)));
        #elif MC_VER > MC_1_20_4
        messageTypeIdString = parameters.type().getIdAsString();
        #endif

        return messageTypeIdString;
    }

    public static DynamicRegistryManager.Immutable getCombinedRegistryManager() {
        return ServerHelper
            .getServer()
            .getCombinedDynamicRegistries()
            .getCombinedRegistryManager();
    }

    public static RegistryWrapper.WrapperLookup getDefaultWrapperLookup() {
        return getCombinedRegistryManager();
    }

    /**
 * The Registry is one of the implementations of RegistryEntryLookup.
 **/
    public static <T> Registry<T> getRegistry(@NotNull RegistryKey<? extends Registry<? extends T>> registryKey) {
        return getCombinedRegistryManager()
            #if MC_VER <= MC_1_21
                .get(registryKey);
            #elif MC_VER > MC_1_21
                .getOrThrow(registryKey);
            #endif
    }

    /**
 * The RegistryEntryLookup is the interface for all types of registries.
 **/
    public static <T> RegistryEntryLookup<T> getRegistryEntryLookup(@NotNull RegistryKey<? extends Registry<? extends T>> registryKey) {
        return getCombinedRegistryManager()
            #if MC_VER <= MC_1_21
                .getWrapperOrThrow(registryKey);
            #elif MC_VER > MC_1_21
                .getOrThrow(registryKey);
            #endif
    }

    public static <T> RegistryKey<T> ofRegistryKey(@NotNull RegistryKey<? extends Registry<T>> registrySpecifier, @NotNull Identifier identifier) {
        return RegistryKey.of(registrySpecifier, identifier);
    }

    public static <T> Optional<RegistryEntry<T>> getRegistryEntry(@NotNull RegistryKey<? extends Registry<T>> registrySpecifier, @NotNull Identifier identifier) {
        Registry<T> registry = getRegistry(registrySpecifier);
        T object = registry.get(identifier);
        RegistryEntry<T> entry = registry.getEntry(object);
        return Optional.ofNullable(entry);
    }

    public static @NotNull Identifier makeIdentifierOrThrow(@NotNull String identifier) {
        #if MC_VER <= MC_1_20_6
        return new Identifier(identifier);
        #elif MC_VER > MC_1_20_6
        return Identifier.of(identifier);
        #endif
    }

    public static Optional<Identifier> makeIdentifier(@NotNull String identifier) {
        try {
            return Optional.of(makeIdentifierOrThrow(identifier));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unused")
    public static <T> Optional<RegistryKey<T>> findRegistryKey(@NotNull RegistryKey<? extends Registry<? extends T>> registrySpecifier, @NotNull T registryValue) {
        return RegistryHelper
            .getRegistry(registrySpecifier)
            .streamEntries()
            .filter(registryEntry -> registryValue.equals(registryEntry.value))
            .findFirst()
            .flatMap(registryEntry -> {
                Optional<RegistryKey<T>> key;
                try {
                    key = registryEntry.getKey();
                } catch (Exception exceptionIfRegistryEntryHasNoRegistryKey) {
                    // NOTE: If the candidate didn't have a key, then we have nothing to return.
                    key = Optional.empty();
                }
                return key;
            });
    }

    public static void ensureIdentifierNamespaceIsFuji(@NotNull Identifier identifier) {
        if (!identifier.getNamespace().equals(Fuji.MOD_ID)) {
            throw new IllegalArgumentException("The namespace of the identifier must be \"fuji\": " + identifier);
        }
    }

}
