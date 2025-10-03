package mod.fuji.core.auxiliary.minecraft;

import mod.fuji.core.document.descriptor.MetaDescriptor;
import mod.fuji.core.document.descriptor.PermissionDescriptor;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.util.Tristate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LuckpermsHelper {

    private static LuckPerms instance;

    private static Optional<LuckPerms> getAPI() {
        if (instance == null) {
            try {
                instance = LuckPermsProvider.get();
            } catch (Exception ignored) {
                // NOTE: The `luckperms` API instance only available when the server is started.
                return Optional.empty();
            }
            return Optional.of(instance);
        }
        return Optional.of(instance);
    }

    /**
 *         1. If you apply loadUser() for a fake-player spawned by carpet-fabric, then the User data will be loaded into the memory by luckperms.
        2. Luckperms will assign the group 'default' for the fake-player, but will never save the User data back to storage.
        3. If you issue <code>/lp user fake_player permission info</code>, luckperms will say there is no User data for this player.

 **/
    private static User loadUser(@NotNull LuckPerms api, @NotNull UUID uuid) {
        UserManager userManager = api.getUserManager();

        /* Load the user from luckperms cache. */
        if (userManager.isLoaded(uuid)) {
            return userManager.getUser(uuid);
        }

        /* Ask to load the user, and wait until the user is loaded. */
        CompletableFuture<User> userFuture = userManager.loadUser(uuid);
        return userFuture.join();
    }

    public static @NotNull Tristate getPermission(@NotNull UUID uuid, @Nullable PermissionDescriptor permissionDescriptor, Object... arguments) {
        /* If luckperms mod is NOT installed, then there is no `string permission`. */
        Optional<LuckPerms> api = getAPI();
        return api
            .map($api -> {
                /* For a `null permission` or `empty permission`, it's im-possible to have it. */
                if (permissionDescriptor == null) return null;
                String permissionString = permissionDescriptor.withArguments(arguments);
                if (permissionString == null || permissionString.isEmpty()) {
                    return null;
                }

                /* Test the permission for the user. */
                User user = loadUser($api, uuid);
                return user
                    .getCachedData()
                    .getPermissionData()
                    .checkPermission(permissionString);
            })
            .orElse(Tristate.UNDEFINED);
    }

    public static boolean hasPermission(@NotNull UUID uuid, @Nullable PermissionDescriptor permissionDescriptor, Object... arguments) {
        return getPermission(uuid, permissionDescriptor, arguments)
            .asBoolean();
    }

    public static <T> @NotNull Optional<T> getMeta(@NotNull UUID uuid, @Nullable MetaDescriptor<T> metaDescriptor, Object... arguments) {
        /* If luckperms is not installed, then there is no `meta`. */
        Optional<LuckPerms> api = getAPI();
        return api
            .map($api -> {
                /* For a `null meta` or `empty meta`, it's im-possible to have it. */
                if (metaDescriptor == null) return null;
                String metaString = metaDescriptor.withArguments(arguments);
                if (metaString == null || metaString.isEmpty()) {
                    return null;
                }

                /* Retrieve the meta for the user. */
                User user = loadUser($api, uuid);
                Optional<? extends T> metaValue = user.getCachedData()
                    .getMetaData()
                    .getMetaValue(metaString, metaDescriptor.valueTransformer);
                return metaValue.orElse(null);
            });
    }

    public static @NotNull String getPrefix(UUID uuid) {
        Optional<LuckPerms> api = getAPI();
        return api
            .map($api -> {
                User user = loadUser($api, uuid);
                return user
                    .getCachedData()
                    .getMetaData()
                    .getPrefix();
            })
            .orElse("");
    }

    public static @NotNull String getSuffix(UUID uuid) {
        Optional<LuckPerms> api = getAPI();
        return api
            .map($api -> {
                User user = loadUser($api, uuid);
                return user
                    .getCachedData()
                    .getMetaData()
                    .getSuffix();
            })
            .orElse("");
    }

}
