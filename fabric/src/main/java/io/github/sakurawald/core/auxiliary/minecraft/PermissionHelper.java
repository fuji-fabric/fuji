package io.github.sakurawald.core.auxiliary.minecraft;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.util.Tristate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@UtilityClass
public class PermissionHelper {

    private static LuckPerms instance;

    private static @Nullable LuckPerms getAPI() {
        if (instance == null) {
            try {
                instance = LuckPermsProvider.get();
            } catch (Exception e) {
                return null;
            }
            return instance;
        }
        return instance;
    }

    /*
     * If you loadUser() for a fake-player spawned by carpet-fabric, then the User data will be loaded into the memory by luckperms.
     * Luckperms will assign the group 'default' for the fake-player, but will never save the User data back to storage.
     * And also, if you issue `/lp user fake_player permission info`, luckperms will say there is no User data for this player.
     */
    private static User loadUser(@NonNull LuckPerms api, UUID uuid) {
        UserManager userManager = api.getUserManager();

        // cache
        if (userManager.isLoaded(uuid)) {
            return userManager.getUser(uuid);
        }

        CompletableFuture<User> userFuture = userManager.loadUser(uuid);
        return userFuture.join();
    }

    public static @NotNull Tristate getPermission(@NotNull UUID uuid, @Nullable String permission) {
        if (permission == null) return Tristate.FALSE;
        if (permission.isBlank()) return Tristate.FALSE;

        LuckPerms api = getAPI();
        if (api == null) {
            return Tristate.UNDEFINED;
        }

        User user = loadUser(api, uuid);
        return user
            .getCachedData()
            .getPermissionData().checkPermission(permission);
    }

    public static boolean hasPermission(UUID uuid, @Nullable String permission) {
        return getPermission(uuid, permission)
            .asBoolean();
    }

    public static <T> @NonNull Optional<T> getMeta(@NotNull UUID uuid, @Nullable String meta, @NonNull Function<String, ? extends T> valueTransformer) {
        if (meta == null) return Optional.empty();

        LuckPerms api = getAPI();
        if (api == null) {
            return Optional.empty();
        }

        User user = loadUser(api, uuid);
        return user.getCachedData()
            .getMetaData()
            .getMetaValue(meta, valueTransformer);
    }

    public static @Nullable String getPrefix(UUID uuid) {
        LuckPerms api = getAPI();
        if (api == null) {
            return null;
        }

        User user = loadUser(api, uuid);

        return user
            .getCachedData()
            .getMetaData()
            .getPrefix();

    }

    public static @Nullable String getSuffix(UUID uuid) {
        LuckPerms api = getAPI();
        if (api == null) {
            return null;
        }

        User user = loadUser(api, uuid);

        return user
            .getCachedData()
            .getMetaData()
            .getSuffix();

    }

}
