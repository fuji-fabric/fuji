package mod.fuji.module.initializer.anti_build;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import mod.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.DocStringProvider;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.core.document.descriptor.PermissionDescriptor;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.PlayerBlockBreakPreEvent;
import mod.fuji.core.event.message.player.PlayerInteractBlockPreEvent;
import mod.fuji.core.event.message.player.PlayerInteractEntityPreEvent;
import mod.fuji.core.event.message.player.PlayerInteractItemPreEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.anti_build.config.model.AntiBuildConfigModel;
import net.luckperms.api.util.Tristate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Document(id = 1751825625507L, value = """
    This module `bans` specific player `actions`.

    The `actions` can be:
    1. Break a specified block.
    2. Place a specified block.
    3. Interact with a specified item.
    4. Interact with a specified block.
    5. Interact with a specified entity.
    6. Attack a specified entity.
    """)
@ColorBox(id = 1751897087633L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ To ban the placement of TNT block.
    Add the `minecraft:tnt` into the `place_block` list in config file.

    ◉ To ban the placement of TNT block, but allow player Alice to use it.
    Assign a `bypass permission` for that player.
    Issue: `/lp user Alice permission set fuji.anti_build.place_block.bypass.minecraft:tnt`.

    ◉ To `ban` or `allow` the player Alice to do a specific action explicitly.
    Assign a `override permission` for that player.

    Issue: `/lp user Alice permission set fuji.anti_build.break_block.override.minecraft:grass_block false`
    It will `ban` the player Alice from breaking `minecraft:grass_block` block.

    Issue: `/lp user Alice permission set fuji.anti_build.break_block.override.minecraft:grass_block true`
    It will `allow` the player Alice to break `minecraft:grass_block` block.

    ◉ To ban the placement of `any` block.
    Add the `*` into the `place_block` list in config file.
    """)
@ColorBox(id = 1753246687639L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Ban the `placement` of `mushroom` blocks in `minecraft:the_end` dimension.
    Issue:
    1. `/lp group default permission set fuji.anti_build.place_block.override.minecraft:red_mushroom false world=the_end`
    2. `/lp group default permission set fuji.anti_build.place_block.override.minecraft:brown_mushroom false world=the_end`
    """)
@TestCase(action = "Test the functionality of this module.", targets = {
    "Check if the anti type hooks remains the identical semantics."
})
public class AntiBuildInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<AntiBuildConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, AntiBuildConfigModel.class);

    @DocStringProvider(id = 1751999560958L, value = """
        To bypass a specific `action` with a specific `id`.
        """)
    private static final PermissionDescriptor ANTI_BUILD_BYPASS_PERMISSION = new PermissionDescriptor("fuji.anti_build.<anti-type>.bypass.<id>", 1751999560958L);

    @DocStringProvider(id = 1752994843864L, value = """
        To `override` a specific `action` with a specific `id`.

        If the permission value is `true`, then it means to `allow (no ban)` this action.
        If the permission value is `false`, then it means to `dis-allow (ban)` this action.
        If the permission value is `undefined`, then it means to `ignore (no process)` this action.
        """)
    private static final PermissionDescriptor ANTI_BUILD_OVERRIDE_PERMISSION = new PermissionDescriptor("fuji.anti_build.<anti-type>.override.<id>", 1752994843864L);

    private static final String IDENTIFIER_WILDCARD_CHARACTER = "*";

    public static void processAntiBuildAction(@Nullable Player player, @NotNull String actionType, @NotNull Set<String> ids, @NotNull String id, @NotNull Runnable canceller, @NotNull Supplier<Boolean> feedbackTrigger) {
        PlayerHelper.Kind.ifServerPlayerEntity(player, () -> {
            /* If this action is allowed. */
            if (isThisActionAllowed(player, actionType, ids, id)) {
                return;
            }

            /* Call the canceller to cancel this action. */
            canceller.run();

            /* Send the feedback text. */
            if (feedbackTrigger.get() && player != null) {
                // NOTE: The `dispenser block` can also place blocks in the world.
                // NOTE: You may see the double message if you install the mod in client-side.
                TextHelper.sendTextByKey(player, "anti_build.disallow");
            }
        });

    }

    public static <T> void processAntiBuildAction(@Nullable Player player, @NotNull String actionType, @NotNull Set<String> ids, @NotNull String id, @NotNull CallbackInfoReturnable<T> cir, @NotNull T cancelWithValue, @NotNull Supplier<Boolean> feedbackTrigger) {
        processAntiBuildAction(player, actionType, ids, id, () -> cir.setReturnValue(cancelWithValue), feedbackTrigger);
    }

    private static boolean isThisActionAllowed(@Nullable Player player, @NotNull String actionType, @NotNull Set<String> ids, @NotNull String id) {
        /* Check the override permission for the player. */
        Tristate overridePermission = getOverridePermission(player, actionType, id);
        if (overridePermission != Tristate.UNDEFINED) {
            return overridePermission.asBoolean();
        }

        /* Check the bypass permission for the player. */
        if (isThisActionAllowedByBypassPermission(player, actionType, id)) {
            return true;
        }

        /* Check the config file for the player. */
        return isThisActionAllowedByConfigurationFile(ids, id);
    }

    private static boolean isThisActionAllowedByConfigurationFile(@NotNull Set<String> ids, @NotNull String id) {
        if (ids.contains(IDENTIFIER_WILDCARD_CHARACTER)) {
            return false;
        }

        return !ids.contains(id);
    }

    private static Tristate getOverridePermission(@Nullable Player player, @NotNull String actionType, @NotNull String id) {
        return Optional.ofNullable(player)
            .map(p -> LuckpermsHelper.getPermission(player.getUUID(), ANTI_BUILD_OVERRIDE_PERMISSION, actionType, id))
            .orElse(Tristate.UNDEFINED);
    }

    private static boolean isThisActionAllowedByBypassPermission(@Nullable Player player, @NotNull String actionType, @NotNull String id) {
        return Optional.ofNullable(player)
            .map(p -> LuckpermsHelper.hasPermission(player.getUUID(), ANTI_BUILD_BYPASS_PERMISSION, actionType, id))
            .orElse(false);
    }

    @EventConsumer(injectorPriority = EventConsumer.LOWEST)
    private static void consumePlayerBlockBreakPreEvent(PlayerBlockBreakPreEvent event) {
        if (event.getCallbackInfoReturnable().isCancelled()) return;
        var config = AntiBuildInitializer.config.model().getAntiTypes().getBreakBlock();
        if (!config.isEnable()) return;

        BlockState blockState = event.getWorld().getBlockState(event.getBlockPos());
        String id = RegistryHelper.getIdAsString(blockState);

        AntiBuildInitializer.processAntiBuildAction(event.getPlayer(), "break_block", config.getId(), id, event.getCallbackInfoReturnable(), false, () -> true);
    }

    @EventConsumer(injectorPriority = EventConsumer.LOWEST)
    private static void consumePlayerInteractItemPreEvent(PlayerInteractItemPreEvent event) {
        if (event.getCallbackInfoReturnable().isCancelled()) return;
        var config = AntiBuildInitializer.config.model().getAntiTypes().getInteractItem();
        if (!config.isEnable()) return;

        String id = RegistryHelper.getIdAsString(event.getItemStack());
        AntiBuildInitializer.processAntiBuildAction(event.getPlayer(), "interact_item", config.getId(), id, event.getCallbackInfoReturnable(), InteractionResult.FAIL, () -> true);
    }

    @EventConsumer(injectorPriority = EventConsumer.LOWEST)
    private static void consumePlayerInteractBlockPreEvent(PlayerInteractBlockPreEvent event) {
        var config = AntiBuildInitializer.config.model().getAntiTypes().getInteractBlock();
        if (!config.isEnable()) return;

        BlockPos blockPos = event.getBlockHitResult().getBlockPos();
        BlockState blockState = event.getWorld().getBlockState(blockPos);
        String id = RegistryHelper.getIdAsString(blockState);

        AntiBuildInitializer.processAntiBuildAction(event.getPlayer(), "interact_block", config.getId(), id, event.getCallbackInfoReturnable(), InteractionResult.FAIL, () -> true);
    }

    @EventConsumer(injectorPriority = EventConsumer.LOWEST)
    private static void consumePlayerInteractEntityPreEvent(PlayerInteractEntityPreEvent event) {
        if (event.getCallbackInfoReturnable().isCancelled()) return;
        var config = AntiBuildInitializer.config.model().getAntiTypes().getInteractEntity();
        if (!config.isEnable()) return;

        String id = RegistryHelper.getIdAsString(event.getEntity());
        AntiBuildInitializer.processAntiBuildAction(event.getPlayer(), "interact_entity", config.getId(), id, event.getCallbackInfoReturnable(), InteractionResult.FAIL, () -> event.getHand() == InteractionHand.MAIN_HAND);
    }
}
