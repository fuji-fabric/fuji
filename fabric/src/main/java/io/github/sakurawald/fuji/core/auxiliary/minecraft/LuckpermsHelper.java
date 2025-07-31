package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import io.github.sakurawald.fuji.core.document.descriptor.MetaDescriptor;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;

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

public class LuckpermsHelper {

    private static LuckPerms instance;
    private static @Nullable LuckPerms getAPI() {
        if (instance == null) {
            try {
                instance = LuckPermsProvider.get();
            } catch (Exception ignored) {
                // NOTE: The `luckperms` API instance only available when the server is started.
                return null;
            }
            return instance;
        }
        return instance;
    }

    /*
     * 1. If you loadUser() for a fake-player spawned by carpet-fabric, then the User data will be loaded into the memory by luckperms.
     * 2. Luckperms will assign the group 'default' for the fake-player, but will never save the User data back to storage.
     * 3. If you issue `/lp user fake_player permission info`, luckperms will say there is no User data for this player.
     */
    private static User loadUser(@NotNull LuckPerms api, UUID uuid) {
        UserManager userManager = api.getUserManager();

        /* Load the user from luckperms cache. */
        if (userManager.isLoaded(uuid)) {
            return userManager.getUser(uuid);
        }

        /* Ask to load the user, and wait until the user is loaded. */
        CompletableFuture<User> userFuture = userManager.loadUser(uuid);
        return userFuture.join();
    }

    public static @NotNull Tristate getPermission(@NotNull UUID uuid, @Nullable PermissionDescriptor permission, Object... arguments) {
        // NOTE: The convention is, own a `positive permission` is a `good` thing.

        /* If luckperms mod is not installed, then there is no `string permission`. */
        LuckPerms api = getAPI();
        if (api == null) {
            return Tristate.UNDEFINED;
        }

        /* For a `null permission`, it's im-possible to have it. */
        if (permission == null) return Tristate.UNDEFINED;
        String permissionString = permission.withArguments(arguments);
        if (permissionString == null || permissionString.isEmpty()) {
            return Tristate.UNDEFINED;
        }

        /* Test the permission for the user. */
        User user = loadUser(api, uuid);
        return user
            .getCachedData()
            .getPermissionData()
            .checkPermission(permissionString);
    }

    public static boolean hasPermission(@NotNull UUID uuid, @Nullable PermissionDescriptor permissionDescriptor, Object... arguments) {
        return getPermission(uuid, permissionDescriptor, arguments)
            .asBoolean();
    }

    public static <T> @NotNull Optional<T> getMeta(@NotNull UUID uuid, @Nullable MetaDescriptor<T> metaDescriptor, Object... arguments) {
        /* If luckperms is not installed, then there is no meta. */
        LuckPerms api = getAPI();
        if (api == null) {
            return Optional.empty();
        }

        /* For a `null meta`, it's im-possible to have it. */
        if (metaDescriptor == null) return Optional.empty();
        String metaString = metaDescriptor.withArguments(arguments);
        if (metaString == null || metaString.isEmpty()) {
            return Optional.empty();
        }

        /* Retrieve the meta for the user. */
        User user = loadUser(api, uuid);
        return user.getCachedData()
            .getMetaData()
            .getMetaValue(metaString, metaDescriptor.valueTransformer);
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
