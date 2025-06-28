package io.github.sakurawald.module.initializer.anti_build;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.PermissionHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.anti_build.config.model.AntiBuildConfigModel;
import io.github.sakurawald.core.structure.descriptor.PermissionDescriptor;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Document("""
    This module allows you to ban `types of actions` for players.

    Currently supported types are:
    1. Break a specified block.
    2. Place a specified block.
    3. Interact with a specified item.
    4. Interact with a specified block.
    5. Interact with a specified entity.
    """)
public class AntiBuildInitializer extends ModuleInitializer {
    public static final BaseConfigurationHandler<AntiBuildConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, AntiBuildConfigModel.class);

    private static final PermissionDescriptor ANTI_BUILD_BYPASS_PERMISSION = new PermissionDescriptor("fuji.anti_build.<anti-type>.bypass.<id>", """
        To bypass a specified `anti type` with specified `id`.

        For example, the permission `fuji.anti_build.place_block.bypass.minecraft:tnt` allows a player to place the TNT block.
        """);

    public static <T> void checkAntiBuild(PlayerEntity player, String antiType, Set<String> ids, String id, CallbackInfoReturnable<T> cir, T cancelWithValue, Supplier<Boolean> shouldSendFeedback) {
        if (shouldWeCancelTheAction(player, antiType, ids, id)) {
            /* Send the cation cancelled message to the player. */
            if (shouldSendFeedback.get()) {
                TextHelper.sendMessageByKey(player, "anti_build.disallow");
            }

            /* Cancel the call with specified value. */
            cir.setReturnValue(cancelWithValue);
        }
    }

    private static boolean shouldWeCancelTheAction(PlayerEntity player, String antiType, Set<String> ids, String id) {
        if (isAllowedByPermission(player, antiType, id)) return false;

        return isDisallowedByConfigurationFile(ids, id);
    }

    private static boolean isDisallowedByConfigurationFile(Set<String> ids, String id) {
        return ids.contains(id)
            || ids.contains("*");
    }

    private static boolean isAllowedByPermission(PlayerEntity player, String antiType, String id) {
        return Optional.ofNullable(player)
            .map(p -> PermissionHelper.hasPermission(player.getUuid(), ANTI_BUILD_BYPASS_PERMISSION, antiType, id))
            .orElse(false);
    }
}
