package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import java.nio.file.Path;
import lombok.SneakyThrows;
#if MC_VER <= MC_1_20_4
#elif MC_VER > MC_1_20_4
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
#endif
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class NbtHelper {

    public static class Walker {

        private static <T extends NbtElement> void setPath(@NotNull NbtCompound root, @NotNull String nbtPath, T value) {
            /* Split the nodes into keys. */
            String[] keys = nbtPath.split("\\.");

            /* Walk down the path until the last key. */
            for (int i = 0; i < keys.length - 1; i++) {
                String node = keys[i];

                // Build the parent node on the fly.
                assert root != null;
                if (!root.contains(node)) {
                    root.put(node, new NbtCompound());
                }

                // Walk along it.
                root = Primitives.getCompound(root, node);
            }

            /* Set the value. */
            String theLastKey = keys[keys.length - 1];
            assert root != null;
            root.put(theLastKey, value);
        }

        private static @Nullable NbtElement readPath(@NotNull NbtCompound root, @NotNull String nbtPath) {
            /* Split the path into keys. */
            String[] nodes = nbtPath.split("\\.");

            /* Walk down the path until the last key. */
            for (int i = 0; i < nodes.length - 1; i++) {
                String node = nodes[i];

                // Check the key.
                assert root != null;
                if (!root.contains(node)) {
                    LogUtil.error("Failed to read specified path {} in nbt {}. (Path not exists)", nbtPath, root);
                    throw new RuntimeException("Failed to read specified path in NBT.");
                }

                // Walk along it.
                root = Primitives.getCompound(root, node);
            }

            /* Get the value. */
            String theLastKey = nodes[nodes.length - 1];
            assert root != null;
            @Nullable NbtElement nbtElement = root.get(theLastKey);
            return nbtElement;
        }

        @SuppressWarnings("unchecked")
        public static <T extends NbtElement> T withNbtElement(@NotNull NbtCompound root, @NotNull String nbtPath, T defaultValue) {
            NbtElement nbtElement = readPath(root, nbtPath);
            if (nbtElement == null) {
                setPath(root, nbtPath, defaultValue);
                return defaultValue;
            }

            return (T) nbtElement;
        }
    }

    public static NbtElement toNbtAllowEmpty(ItemStack stack, RegistryWrapper.WrapperLookup wrapperLookup) {
        /* Return empty NBT if item stack is empty. */
        if (stack.isEmpty()) {
            return new NbtCompound();
        }

        #if MC_VER <= MC_1_21
            return StackHelper.encodeAllowEmpty(stack, wrapperLookup);
        #elif MC_VER > MC_1_21
            return StackHelper.toNbt(stack, wrapperLookup, new NbtCompound());
        #endif
    }

    public static ItemStack fromNbtOrEmpty(RegistryWrapper.WrapperLookup wrapperLookup, NbtCompound nbtCompound) {
        /* Return empty item stack if NBT is empty. */
        if (nbtCompound.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return StackHelper.fromNbt(wrapperLookup, nbtCompound).orElse(ItemStack.EMPTY);
    }


    public static NbtList writeSlotsNode(@NotNull NbtList node, @NotNull List<ItemStack> stackList) {
        for (ItemStack stack : stackList) {
            node.add(toNbtAllowEmpty(stack, RegistryHelper.getDefaultWrapperLookup()));
        }
        return node;
    }

    public static @NotNull List<ItemStack> readSlotsNode(@Nullable NbtList node) {
        if (node == null) return new ArrayList<>();

        List<ItemStack> ret = new ArrayList<>();
        for (int i = 0; i < node.size(); i++) {
            ret.add(fromNbtOrEmpty(RegistryHelper.getDefaultWrapperLookup(), Primitives.getCompound(node, i)));
        }
        return ret;
    }

    public static class Storage {

        @SneakyThrows
        private static void writeNbtFile(@NotNull NbtCompound nbt, @NotNull Path filePath) {
            #if MC_VER <= MC_1_20_2
            NbtIo.write(nbt, filePath.toFile());
            #elif MC_VER > MC_1_20_2
            NbtIo.write(nbt, path);
            #endif
        }

        @SneakyThrows
        private static NbtCompound readNbtFile(Path filePath) {
            #if MC_VER <= MC_1_20_2
            return NbtIo.read(filePath.toFile());
            #elif MC_VER > MC_1_20_2
            return NbtIo.read(path);
            #endif
        }

        public static <T> T withNbtFileAndGettingReturnValue(@NotNull Path filePath, @NotNull Function<NbtCompound, T> function) {
            /* Ensure file exists. */
            if (Files.notExists(filePath)) {
                writeNbtFile(new NbtCompound(), filePath);
            }

            /* Read the file. */
            NbtCompound readNbt = readNbtFile(filePath);
            if (readNbt == null) {
                LogUtil.error("Failed to read the nbt file in {}", filePath);
                throw new AbortCommandExecutionException();
            }

            /* Call the consumer. */
            T value = function.apply(readNbt);

            /* Always write the data back, whether it's a destructive operation or not. */
            writeNbtFile(readNbt, filePath);

            /* Transfer the return value from closure function to surrounding env. */
            return value;
        }

        public static void withNbtFile(@NotNull Path filePath, @NotNull Consumer<NbtCompound> function) {
            withNbtFileAndGettingReturnValue(filePath, (root) -> {
                function.accept(root);
                // Discard the return value.
                return null;
            });
        }
    }

    public static void withNbt(ItemStack stack, Consumer<NbtCompound> nbtConsumer) {
        NbtCompound targetNbt = NbtHelper.getNbt(stack);
        if (targetNbt == null) {
            targetNbt = new NbtCompound();
        }

        nbtConsumer.accept(targetNbt);

        NbtHelper.setNbt(stack, targetNbt);
    }

    public static @Nullable NbtCompound getNbt(@NotNull ItemStack stack) {
        #if MC_VER <= MC_1_20_4
        return stack.getNbt();
        #elif MC_VER > MC_1_20_4
        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        return nbtComponent == null ? null : nbtComponent.copyNbt();
        #endif
    }

    public static void setNbt(@NotNull ItemStack stack, @NotNull NbtCompound newNbt) {
        #if MC_VER <= MC_1_20_4
        stack.setNbt(newNbt);
        #elif MC_VER > MC_1_20_4
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(newNbt));
        #endif
    }

    public static class Primitives {

        public static @Nullable String getString(NbtCompound root, String key) {
            #if MC_VER <= MC_1_21_4
            return root.getString(key);
            #elif MC_VER >= MC_1_21_5
            return root.getString(key).orElse(null);
            #endif
        }

        public static @Nullable NbtCompound getCompound(NbtCompound root, String key) {
            #if MC_VER <= MC_1_21_4
            return root.getCompound(key);
            #elif MC_VER >= MC_1_21_5
            return root.getCompound(key).get();
            #endif
        }

        public static @Nullable NbtCompound getCompound(NbtList list, int index) {
            #if MC_VER <= MC_1_21_4
            return list.getCompound(index);
            #elif MC_VER >= MC_1_21_5
            return list.getCompound(index).get();
            #endif
        }

        public static int getInt(NbtCompound root, String key) {
            #if MC_VER <= MC_1_21_4
            return root.getInt(key);
            #elif MC_VER >= MC_1_21_5
            return root.getInt(key).get();
            #endif
        }

        public static float getFloat(NbtCompound root, String key) {
            #if MC_VER <= MC_1_21_4
            return root.getFloat(key);
            #elif MC_VER >= MC_1_21_5
            return root.getFloat(key).get();
            #endif
        }

        public static double getDouble(NbtCompound root, String key) {
            #if MC_VER <= MC_1_21_4
            return root.getDouble(key);
            #elif MC_VER >= MC_1_21_5
            return root.getDouble(key).get();
            #endif
        }
    }

}
