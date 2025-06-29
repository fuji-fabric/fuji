package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import io.github.sakurawald.fuji.core.document.descriptor.MetaDescriptor;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import lombok.NonNull;
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

    public static @NotNull Tristate getPermission(@NotNull UUID uuid, @Nullable PermissionDescriptor permission, Object... arguments
    ) {
        if (permission == null) return Tristate.FALSE;

        LuckPerms api = getAPI();
        if (api == null) {
            return Tristate.UNDEFINED;
        }

        User user = loadUser(api, uuid);
        String permissionString = permission.withArguments(arguments);
        if (permissionString == null || permissionString.isEmpty()) {
            return Tristate.FALSE;
        }

        return user
            .getCachedData()
            .getPermissionData().checkPermission(permissionString);
    }

    public static boolean hasPermission(UUID uuid, @Nullable PermissionDescriptor permissionDescriptor, Object... arguments) {
        return getPermission(uuid, permissionDescriptor, arguments)
            .asBoolean();
    }

    public static <T> @NonNull Optional<T> getMeta(@NotNull UUID uuid, @Nullable MetaDescriptor<T> metaDescriptor, Object... arguments) {
        if (metaDescriptor == null) return Optional.empty();

        LuckPerms api = getAPI();
        if (api == null) {
            return Optional.empty();
        }

        String meta = metaDescriptor.withArguments(arguments);

        User user = loadUser(api, uuid);
        return user.getCachedData()
            .getMetaData()
            .getMetaValue(meta, metaDescriptor.valueTransformer);
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
