package mod.fuji.module.initializer.anti_build;

import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.DocStringProvider;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.PlayerBlockBreakPreEvent;
import mod.fuji.core.event.message.player.PlayerInteractBlockPreEvent;
import mod.fuji.core.event.message.player.PlayerInteractEntityPreEvent;
import mod.fuji.core.event.message.player.PlayerInteractItemPreEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.anti_build.config.model.AntiBuildConfigModel;
import mod.fuji.core.document.descriptor.PermissionDescriptor;
import net.luckperms.api.util.Tristate;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Document(id = 1751825625507L, value = """
    This module allows restricting specific player `actions`.

    Currently supported `action types` include:
    1. Break a specified block.
    2. Place a specified block.
    3. Interact with a specified item.
    4. Interact with a specified block.
    5. Interact with a specified entity.
    6. Attack a specified entity.
    """)
@ColorBox(id = 1751896813134L, color = ColorBox.ColorBoxTypes.TIP, value = """
    Read the document to see the definition of `identifier` in Minecraft.
    """)
@ColorBox(id = 1751896904529L, color = ColorBox.ColorBoxTypes.TIP, value = """
    Use the `command suggestion` from `luckperms` mod to see the supported types by this module.
    """)
@ColorBox(id = 1751897087633L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ To ban the placement of TNT block:
    Just add the `minecraft:tnt` into the `place_block` list in config file.

    ◉ To ban the placement of TNT block, but allow player Alice to use it.
    Grant a `bypass permission` for that player: `/lp user Alice permission set fuji.anti_build.place_block.bypass.minecraft:tnt`.

    ◉ To assign a override permission for a player explicitly.
    Issue: `/lp user Alice permission set fuji.anti_build.break_block.override.minecraft:grass_block false`
    This will `dis-allow` the player Alice to `break a minecraft:grass_block block`.

    ◉ Dis-allow to place `any` blocks.
    Use `*` as the wildcard character, put it into the `place_block` list.
    """)
@ColorBox(id = 1753246687639L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Ban the `placement` of `mushroom` in `minecraft:the_end` dimension.
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
        To bypass a specific `anti type` with a specific `id`.

        For example, the permission `fuji.anti_build.place_block.bypass.minecraft:tnt` allows a player to place the TNT block.
        """)
    private static final PermissionDescriptor ANTI_BUILD_BYPASS_PERMISSION = new PermissionDescriptor("fuji.anti_build.<anti-type>.bypass.<id>", 1751999560958L);

    @DocStringProvider(id = 1752994843864L, value = """
        To `override` a specific `anti type` with a specific `id`.

        If the permission value is `true`, then it means `allow` this action.
        If the permission value is `false`, then it means `dis-allow` this action.
        If the permission value is `undefined`, then it means this action is `ignored`.
        """)
    private static final PermissionDescriptor ANTI_BUILD_OVERRIDE_PERMISSION = new PermissionDescriptor("fuji.anti_build.<anti-type>.override.<id>", 1752994843864L);

    public static void processAntiBuild(@Nullable PlayerEntity player, @NotNull String antiType, @NotNull Set<String> ids, @NotNull String id, @NotNull Runnable canceller, @NotNull Supplier<Boolean> feedbackTrigger) {
        PlayerHelper.Kind.withServerPlayerEntity(player,() -> {
            // NOTE: This method will NOT be called for a dispenser block.
            if (isThisActionAllowed(player, antiType, ids, id)) {
                return;
            }

            /* Call the canceller to cancel this event. */
            canceller.run();

            /* Send the cation cancelled message to the player. */
            if (feedbackTrigger.get() && player != null) {
                // NOTE: The `dispenser block` can also place blocks in the world.
                // NOTE: You may see the double message if you install the mod in client-side.
                TextHelper.sendTextByKey(player, "anti_build.disallow");
            }
        });

    }

    public static <T> void processAntiBuild(@Nullable PlayerEntity player, @NotNull String antiType, @NotNull Set<String> ids, @NotNull String id, @NotNull CallbackInfoReturnable<T> cir, @NotNull T cancelWithValue, @NotNull Supplier<Boolean> feedbackTrigger) {
        processAntiBuild(player, antiType, ids, id, () -> cir.setReturnValue(cancelWithValue), feedbackTrigger);
    }

    private static boolean isThisActionAllowed(@Nullable PlayerEntity player, @NotNull String antiType, @NotNull Set<String> ids, @NotNull String id) {
        /* Check the override permission for the player. */
        Tristate overridePermission = getOverridePermission(player, antiType, id);
        if (overridePermission != Tristate.UNDEFINED) {
            return overridePermission.asBoolean();
        }

        /* Check the bypass permission for the player. */
        if (isAllowedByBypassPermission(player, antiType, id)) {
            return true;
        }

        /* Check the config file for the player. */
        return isAllowedByConfigurationFile(ids, id);
    }

    private static boolean isAllowedByConfigurationFile(@NotNull Set<String> ids, @NotNull String id) {
        return !ids.contains(id)
            && !ids.contains("*");
    }

    private static Tristate getOverridePermission(@Nullable PlayerEntity player, @NotNull String antiType, @NotNull String id) {
        return Optional.ofNullable(player)
            .map(p -> LuckpermsHelper.getPermission(player.getUuid(), ANTI_BUILD_OVERRIDE_PERMISSION, antiType, id))
            .orElse(Tristate.UNDEFINED);
    }

    private static boolean isAllowedByBypassPermission(@Nullable PlayerEntity player, @NotNull String antiType, @NotNull String id) {
        return Optional.ofNullable(player)
            .map(p -> LuckpermsHelper.hasPermission(player.getUuid(), ANTI_BUILD_BYPASS_PERMISSION, antiType, id))
            .orElse(false);
    }

    @EventConsumer(injectorPriority = EventConsumer.LOWEST)
    private static void consumePlayerBlockBreakPreEvent(PlayerBlockBreakPreEvent event) {
        if (event.getCallbackInfoReturnable().isCancelled()) return;
        var config = AntiBuildInitializer.config.model().getAntiTypes().getBreakBlock();
        if (!config.isEnable()) return;

        BlockState blockState = event.getWorld().getBlockState(event.getBlockPos());
        String id = RegistryHelper.getIdAsString(blockState);

        AntiBuildInitializer.processAntiBuild(event.getPlayer(), "break_block", config.getId(), id, event.getCallbackInfoReturnable(), false, () -> true);
    }

    @EventConsumer(injectorPriority = EventConsumer.LOWEST)
    private static void consumePlayerInteractItemPreEvent(PlayerInteractItemPreEvent event) {
        if (event.getCallbackInfoReturnable().isCancelled()) return;
        var config = AntiBuildInitializer.config.model().getAntiTypes().getInteractItem();
        if (!config.isEnable()) return;

        String id = RegistryHelper.getIdAsString(event.getItemStack());
        AntiBuildInitializer.processAntiBuild(event.getPlayer(), "interact_item", config.getId(), id, event.getCallbackInfoReturnable(), ActionResult.FAIL, () -> true);
    }

    @EventConsumer(injectorPriority = EventConsumer.LOWEST)
    private static void consumePlayerInteractBlockPreEvent(PlayerInteractBlockPreEvent event) {
        var config = AntiBuildInitializer.config.model().getAntiTypes().getInteractBlock();
        if (!config.isEnable()) return;

        BlockPos blockPos = event.getBlockHitResult().getBlockPos();
        BlockState blockState = event.getWorld().getBlockState(blockPos);
        String id = RegistryHelper.getIdAsString(blockState);

        AntiBuildInitializer.processAntiBuild(event.getPlayer(), "interact_block", config.getId(), id, event.getCallbackInfoReturnable(), ActionResult.FAIL, () -> true);
    }

    @EventConsumer(injectorPriority = EventConsumer.LOWEST)
    private static void consumePlayerInteractEntityPreEvent(PlayerInteractEntityPreEvent event) {
        if (event.getCallbackInfoReturnable().isCancelled()) return;
        var config = AntiBuildInitializer.config.model().getAntiTypes().getInteractEntity();
        if (!config.isEnable()) return;

        String id = RegistryHelper.getIdAsString(event.getEntity());
        AntiBuildInitializer.processAntiBuild(event.getPlayer(), "interact_entity", config.getId(), id, event.getCallbackInfoReturnable(), ActionResult.FAIL, () -> event.getHand() == Hand.MAIN_HAND);
    }
}
