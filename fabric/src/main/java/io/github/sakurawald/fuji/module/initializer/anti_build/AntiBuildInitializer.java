package io.github.sakurawald.fuji.module.initializer.anti_build;

import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.anti_build.config.model.AntiBuildConfigModel;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Document(id = 1751825625507L, value = """
    This module allows you to ban `types of actions` for players.

    Currently supported types are:
    1. Break a specified block.
    2. Place a specified block.
    3. Interact with a specified item.
    4. Interact with a specified block.
    5. Interact with a specified entity.
    """)

@ColorBox(id = 1751896813134L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    Read the document to see the definition of `identifier` in Minecraft.
    """)

@ColorBox(id = 1751896904529L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    Use the `command suggestion` from `luckperms` mod to see the supported types by this module.
    """)

@ColorBox(id = 1751897087633L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    To ban the placement of TNT block:
    Just add the `minecraft:tnt` into the `place_block` list.
    """)

@ColorBox(id = 1751897135695L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    To ban the placement of TNT block, but allow a specific player to use it.
    Grant a `bypass permission` for that player: `/lp user \\<player\\> permission set fuji.anti_build.place_block.bypass.minecraft:tnt`.
    """)

@ColorBox(id = 1751897263346L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    Dis-allow to place any blocks.
    Use `*` as the wildcard character, put it into the `place_block` list.
    """)

public class AntiBuildInitializer extends ModuleInitializer {
    public static final BaseConfigurationHandler<AntiBuildConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, AntiBuildConfigModel.class);

    @DocStringProvider(id = 1751999560958L, value = """
        To bypass a specific `anti type` with a specific `id`.

        For example, the permission `fuji.anti_build.place_block.bypass.minecraft:tnt` allows a player to place the TNT block.
        """)
    private static final PermissionDescriptor ANTI_BUILD_BYPASS_PERMISSION = new PermissionDescriptor("fuji.anti_build.<anti-type>.bypass.<id>", 1751999560958L);

    public static <T> void checkAntiBuild(PlayerEntity player, String antiType, Set<String> ids, String id, CallbackInfoReturnable<T> cir, T cancelWithValue, Supplier<Boolean> shouldSendFeedback) {
        if (shouldWeCancelTheAction(player, antiType, ids, id)) {
            /* Send the cation cancelled message to the player. */
            if (shouldSendFeedback.get()) {
                TextHelper.sendTextByKey(player, "anti_build.disallow");
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
            .map(p -> LuckpermsHelper.hasPermission(player.getUuid(), ANTI_BUILD_BYPASS_PERMISSION, antiType, id))
            .orElse(false);
    }
}
