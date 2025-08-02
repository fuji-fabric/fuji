package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.SneakyThrows;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NbtHelper {

    public static class Walker {

        private static void ensureKeysIsNotEmpty(@NotNull String nbtPath, String[] keys) {
            if (keys.length == 0) {
                throw new IllegalArgumentException("Failed to split the nbtPath %s".formatted(nbtPath));
            }
        }

        private static <T extends NbtElement> void setPath(@NotNull NbtCompound root, @NotNull String nbtPath, T value) {
            /* Split the nodes into keys. */
            String[] keys = nbtPath.split("\\.", -1);
            ensureKeysIsNotEmpty(nbtPath, keys);

            /* Walk down the path until the last key. */
            for (int i = 0; i < keys.length - 1; i++) {
                String node = keys[i];

                // Build the parent node on the fly.
                assert root != null;
                if (!root.contains(node)) {
                    root.put(node, new NbtCompound());
                }

                // Walk along it.
                root = Primitives.getCompound(root, node).get();
            }

            /* Set the value. */
            String theLastKey = keys[keys.length - 1];
            assert root != null;
            root.put(theLastKey, value);
        }

        private static @Nullable NbtElement readPath(@NotNull NbtCompound root, @NotNull String nbtPath) {
            /* Split the path into keys. */
            String[] nodes = nbtPath.split("\\.", -1);
            ensureKeysIsNotEmpty(nbtPath, nodes);

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
                root = Primitives.getCompound(root, node).get();
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

    public static class Storage {

        @SneakyThrows
        private static void writeNbtFile(@NotNull NbtCompound nbt, @NotNull Path filePath) {
            #if MC_VER <= MC_1_20_2
            NbtIo.write(nbt, filePath.toFile());
            #elif MC_VER > MC_1_20_2
            NbtIo.write(nbt, filePath);
            #endif
        }

        @SneakyThrows
        private static NbtCompound readNbtFile(Path filePath) {
            #if MC_VER <= MC_1_20_2
            return NbtIo.read(filePath.toFile());
            #elif MC_VER > MC_1_20_2
            return NbtIo.read(filePath);
            #endif
        }

        public static <T> T withNbtFileAndGetReturnValue(@NotNull Path filePath, @NotNull Function<NbtCompound, T> function) {
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
            withNbtFileAndGetReturnValue(filePath, (root) -> {
                function.accept(root);
                // Discard the return value.
                return null;
            });
        }
    }

    public static class Primitives {

        public static Optional<String> getString(@NotNull NbtCompound root, @NotNull String key) {
            #if MC_VER <= MC_1_21_4
            return Optional.ofNullable(root.getString(key));
            #elif MC_VER >= MC_1_21_5
            return root.getString(key);
            #endif
        }

        public static Optional<NbtCompound> getCompound(@NotNull NbtCompound root, @NotNull String key) {
            #if MC_VER <= MC_1_21_4
            return Optional.ofNullable(root.getCompound(key));
            #elif MC_VER >= MC_1_21_5
            return root.getCompound(key);
            #endif
        }

        public static Optional<NbtCompound> getCompound(@NotNull NbtList list, int index) {
            #if MC_VER <= MC_1_21_4
            return Optional.ofNullable(list.getCompound(index));
            #elif MC_VER >= MC_1_21_5
            return list.getCompound(index);
            #endif
        }

        public static Optional<Integer> getInt(@NotNull NbtCompound root, @NotNull String key) {
            #if MC_VER <= MC_1_21_4
            return Optional.of(root.getInt(key));
            #elif MC_VER >= MC_1_21_5
            return root.getInt(key);
            #endif
        }

        public static Optional<Float> getFloat(@NotNull NbtCompound root, @NotNull String key) {
            #if MC_VER <= MC_1_21_4
            return Optional.of(root.getFloat(key));
            #elif MC_VER >= MC_1_21_5
            return root.getFloat(key);
            #endif
        }

        public static Optional<Double> getDouble(@NotNull NbtCompound root, @NotNull String key) {
            #if MC_VER <= MC_1_21_4
            return Optional.of(root.getDouble(key));
            #elif MC_VER >= MC_1_21_5
            return root.getDouble(key);
            #endif
        }
    }

}
