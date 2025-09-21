package mod.fuji.core.auxiliary.minecraft;

import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.command.exception.AbortCommandExecutionException;
import mod.fuji.core.document.annotation.ForDeveloper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
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

        private static String @NotNull [] splitNbtPath(@NotNull String nbtPath) {
            return nbtPath.split("\\.", -1);
        }


        private static @NotNull IllegalStateException makeNbtElementTypeMismatchException(@NotNull String nbtPath) {
            return new IllegalStateException("The field in nbt path %s is not type of NbtCompound.".formatted(nbtPath));
        }

        private static <T extends NbtElement> void setNbtPath(@NotNull NbtCompound root, @NotNull String nbtPath, @NotNull T newValue) {
            /* Split the nodes into keys. */
            String[] keys = splitNbtPath(nbtPath);
            ensureKeysIsNotEmpty(nbtPath, keys);

            /* Walk down the path until the last key. */
            for (int i = 0; i < keys.length - 1; i++) {
                String node = keys[i];

                // Build the parent node on the fly.
                if (!root.contains(node)) {
                    root.put(node, new NbtCompound());
                }

                // Walk along it.
                root = Primitives
                    .getCompound(root, node)
                    .orElseThrow(() -> {
                        LogUtil.error("Failed to set the value for specified nbt path: nbtPath = {}, newValue = {}", nbtPath, newValue);
                        return makeNbtElementTypeMismatchException(nbtPath);
                    });
            }

            /* Set the value. */
            String theLastKey = keys[keys.length - 1];
            root.put(theLastKey, newValue);
        }

        private static @Nullable NbtElement readNbtPath(@NotNull NbtCompound root, @NotNull String nbtPath) {
            /* Split the path into keys. */
            String[] keys = splitNbtPath(nbtPath);
            ensureKeysIsNotEmpty(nbtPath, keys);

            /* Walk down the path until the last key. */
            for (int i = 0; i < keys.length - 1; i++) {
                String node = keys[i];

                // Check the key.
                if (!root.contains(node)) {
                    LogUtil.error("Failed to read the value of specified nbt path: root = {}, nbtPath = {} (There is no `{}` key)", root, nbtPath, node);
                    throw new IllegalStateException();
                }

                // Walk along it.
                root = Primitives
                    .getCompound(root, node)
                    .orElseThrow(() -> {
                        LogUtil.error("Failed to read the value of specified nbt path: nbtPath = {}", nbtPath);
                        return makeNbtElementTypeMismatchException(nbtPath);
                    });
            }

            /* Get the value. */
            String theLastKey = keys[keys.length - 1];
            @Nullable NbtElement nbtElement = root.get(theLastKey);
            return nbtElement;
        }

        @SuppressWarnings("unchecked")
        public static <T extends NbtElement> T getOrCreateNbtElement(@NotNull NbtCompound root, @NotNull String nbtPath, @NotNull T defaultValue) {
            NbtElement nbtElement = readNbtPath(root, nbtPath);
            if (nbtElement == null) {
                setNbtPath(root, nbtPath, defaultValue);
                return defaultValue;
            }

            return (T) nbtElement;
        }
    }

    @ForDeveloper("""
        You should declare the throws IOException for low-level read/write operations.
        The high level layers should handle these exceptions, or just @SneakyThrow to bypass them.
        """)
    public static class Storage {

        private static void writeNbtFile(@NotNull NbtCompound root, @NotNull Path filePath) throws IOException {
            #if MC_VER <= MC_1_20_2
            NbtIo.write(root, filePath.toFile());
            #elif MC_VER > MC_1_20_2
            NbtIo.write(root, filePath);
            #endif
        }

        private static @Nullable NbtCompound readNbtFile(@NotNull Path filePath) throws IOException {
            #if MC_VER <= MC_1_20_2
            return NbtIo.read(filePath.toFile());
            #elif MC_VER > MC_1_20_2
            return NbtIo.read(filePath);
            #endif
        }

        public static <T> T withNbtFile(@NotNull Path filePath, @NotNull Function<NbtCompound, T> function) throws IOException {
            /* Write a default file if no file exists. */
            if (Files.notExists(filePath)) {
                writeNbtFile(new NbtCompound(), filePath);
            }

            /* Read the file. */
            NbtCompound nbt = readNbtFile(filePath);
            if (nbt == null) {
                LogUtil.error("Failed to read the nbt file in {}", filePath);
                throw new AbortCommandExecutionException();
            }

            /* Call the consumer. */
            T value = function.apply(nbt);

            /* Always write the data back, whether it's a destructive operation or not. */
            writeNbtFile(nbt, filePath);

            /* Transfer the return value from closure function to surrounding env. */
            return value;
        }

        public static void withNbtFile(@NotNull Path filePath, @NotNull Consumer<NbtCompound> function) throws IOException {
            withNbtFile(filePath, (root) -> {
                function.accept(root);
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
