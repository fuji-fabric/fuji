package mod.fuji.module.initializer.launcher;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.EntityCollection;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.entity.LivingEntityDamageEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;


@Document(id = 1756347408511L, value = """
    Launch a target `entity` in specified `direction` and `power`.
    """)
@ColorBox(id = 1757527645811L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Create a `jump pad` that launches players when stepped on.
    You can integrate with `command_attachment` module.
    Issue: `/command-attachment attach-block-one ~ ~ ~ --interactType STEP_ON \\<command\\>`

    ◉ Create a `knock-back stick` that kick the entities around you.
    Issue: `/command-attachment attach-item-one launch at %player:name% @e[type=!minecraft:player,distance=..8] 30 1`
    """)
@ColorBox(id = 1756440410476L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Use a lower `angle` for `fast horizontal movement`.
    1. `/launch facing @s 15 1`
    2. `/launch facing @s 15 3.9`
    <green>The `power` is clamped in `[-3.9, +3.9]`

    ◉ Use a median `angle` for `balanced horizontal and vertical movement`.
    1. `/launch facing @s 30 3.9`
    2. `/launch facing @s 45 3.9`

    ◉ Use a higher `angle` for a `rocket launcher` effect.
    Issue: `/launch facing @s 60 3.9`
    <green>TIP: Remember to bring your `elytra`.

    ◉ Use a `vertical angle` for a `trampoline` effect.
    Issue: `/launch facing @s 90 1`

    ◉ Use a `positive power` for a `push` effect.
    Issue: `/launch facing @s 0 1`

    ◉ Use a `negative power` for a `pull` effect.
    Issue: `/launch facing @s 0 -1`

    ◉ Use another entity's perspective as the direction, to `kick` the target entity.
    Issue: `/launch at @s @e[type=!minecraft:player,distance=..8] 30 1`
    """)
public class LauncherInitializer extends ModuleInitializer {

    public static Set<Entity> LAUNCHED_ENTITIES = new HashSet<>();

    private static void launchEntityFacing(@NotNull Entity player, float angle, double power) {
        launchEntity(player, player.getYRot(), angle, power);
    }

    private static void launchEntityAt(@NotNull Entity player, @NotNull Entity at, float angle, double power) {
        launchEntity(player, at.getYRot(), angle, power);
    }

    public static void launchEntity(@NotNull Entity entity, float perspectiveYaw, float perspectivePitch, double power) {
        /* Compute yaw and pitch. */
        float yaw = perspectiveYaw * ((float) Math.PI / 180F);
        float pitch = perspectivePitch * ((float) Math.PI / 180F);

        /* Compute directional vector from yaw + pitch. */
        double x = -Math.sin(yaw) * Math.cos(pitch);
        double y = Math.sin(pitch);
        double z = Math.cos(yaw) * Math.cos(pitch);

        /* Normalize. */
        double length = Math.sqrt(x * x + y * y + z * z);
        x /= length;
        y /= length;
        z /= length;

        /* Apply velocity scaled by power. */
        power = Mth.clamp(power, -3.9F, 3.9F);
        entity.setDeltaMovement(x * power, y * power, z * power);

        /* Mark velocity as modified. */
        entity.hurtMarked = true;
    }

    @CommandNode("launch facing")
    @CommandRequirement(level = 4)
    private static int $launch(@CommandSource CommandSourceStack source, EntityCollection target, float angle, double power) {
        target
            .getValue()
            .forEach(entity -> {
                LAUNCHED_ENTITIES.add(entity);
                launchEntityFacing(entity, angle, power);
            });
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("launch at")
    @CommandRequirement(level = 4)
    private static int $launch(@CommandSource CommandSourceStack source, Entity at, EntityCollection target, float angle, double power) {
        target
            .getValue()
            .forEach(entity -> {
                LAUNCHED_ENTITIES.add(entity);
                launchEntityAt(entity, at, angle, power);
            });
        return CommandHelper.Return.SUCCESS;
    }

    @EventConsumer
    private static void safelyLanding(LivingEntityDamageEvent event) {
        Entity entity = event.getLivingEntity();
        if (!LauncherInitializer.LAUNCHED_ENTITIES.contains(entity)) {
            return;
        }

        /* Compute the boolean value. */
        DamageSource damageSource = event.getDamageSource();
        boolean immuneToThisDamageType = damageSource
            .typeHolder()
            .unwrapKey()
            .map(key -> key.equals(DamageTypes.FALL))
            .orElse(false);

        /* Cancel the damage by flag. */
        if (immuneToThisDamageType) {
            LauncherInitializer.LAUNCHED_ENTITIES.remove(entity);
            event.setDamage(0);
        }
    }


}
