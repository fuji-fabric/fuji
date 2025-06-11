package io.github.sakurawald.core.auxiliary.minecraft;

import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.command.exception.AbortCommandExecutionException;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@UtilityClass
public class NbtHelper {

    private static <T extends NbtElement> void setPath(@NotNull NbtCompound root, @NotNull String path, T value) {
        /* walk the path */
        String[] nodes = path.split("\\.");
        for (int i = 0; i < nodes.length - 1; i++) {
            String node = nodes[i];

            if (!root.contains(node)) {
                root.put(node, new NbtCompound());
            }

            // root is ensured not-null, just get it.
            root = getCompound(root, node);
        }

        /* set the value */
        String key = nodes[nodes.length - 1];
        root.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T extends NbtElement> T withNbtElement(@NotNull NbtCompound root, @NotNull String path, T orElse) {
        if (readPath(root, path) == null) {
            setPath(root, path, orElse);
        }

        return (T) readPath(root, path);
    }

    private static @Nullable NbtElement readPath(@NotNull NbtCompound root, @NotNull String path) {
        // search the path
        String[] nodes = path.split("\\.");
        for (int i = 0; i < nodes.length - 1; i++) {
            String node = nodes[i];

            if (!root.contains(node)) {
                LogUtil.error("nbt {} don't has path {}", root, path);
            }

            root = getCompound(root, node);
        }

        // get the value
        String key = nodes[nodes.length - 1];
        return root.get(key);
    }

    public static NbtList writeSlotsNode(@NotNull NbtList node, @NotNull List<ItemStack> itemStackList) {
        for (ItemStack stack : itemStackList) {
            node.add(toNbtAllowEmpty(stack, RegistryHelper.getDefaultWrapperLookup()));
        }
        return node;
    }

    public NbtElement toNbtAllowEmpty(ItemStack stack, RegistryWrapper.WrapperLookup wrapperLookup) {
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
        if (nbtCompound.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return StackHelper.fromNbt(wrapperLookup, nbtCompound).orElse(ItemStack.EMPTY);
    }


    public static @NotNull List<ItemStack> readSlotsNode(@Nullable NbtList node) {
        if (node == null) return new ArrayList<>();

        List<ItemStack> ret = new ArrayList<>();
        for (int i = 0; i < node.size(); i++) {
            ret.add(fromNbtOrEmpty(RegistryHelper.getDefaultWrapperLookup(), getCompound(node, i)));
        }
        return ret;
    }

    public static void withNbtFile(@NotNull Path path, @NotNull Consumer<NbtCompound> function) {
        // discard the return value
        withNbtFileAndGettingReturnValue(path, (root) -> {
            function.accept(root);
            return null;
        });
    }

    @SneakyThrows(IOException.class)
    public static <T> T withNbtFileAndGettingReturnValue(@NotNull Path path, @NotNull Function<NbtCompound, T> function) {
        /* make file if not exists */
        if (Files.notExists(path)) {
            NbtIo.write(new NbtCompound(), path);
        }

        /* read the file */
        NbtCompound read = NbtIo.read(path);
        if (read == null) {
            LogUtil.error("failed to read the nbt file in {}", path);
            throw new AbortCommandExecutionException();
        }

        /* call the consumer */
        T value = function.apply(read);

        /* always write the data back, whether it's a destructive operation or not */
        NbtIo.write(read, path);

        /* return the useful value to outer space */
        return value;
    }

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

    public static @Nullable NbtCompound getNbt(ItemStack stack) {
        #if MC_VER <= MC_1_20_4
            return stack.getNbt();
        #elif MC_VER > MC_1_20_4
            return stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt();
        #endif
    }

    public void withNbt(ItemStack stack, Consumer<NbtCompound> nbtConsumer) {
        NbtCompound targetNbt = NbtHelper.getNbt(stack);
        if (targetNbt == null) {
            targetNbt = new NbtCompound();
        }

        nbtConsumer.accept(targetNbt);

        NbtHelper.setNbt(stack, targetNbt);
    }

    public static void setNbt(ItemStack stack, NbtCompound newNbt) {
        #if MC_VER <= MC_1_20_4
            stack.setNbt(newNbt);
        #elif MC_VER > MC_1_20_4
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(newNbt));
        #endif
    }
}
