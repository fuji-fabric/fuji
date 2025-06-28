package io.github.sakurawald.module.initializer.afk;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.annotation.CommandTarget;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.core.extension.PlayerCombatExtension;
import io.github.sakurawald.core.structure.descriptor.annotation.ColorBox;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.afk.accessor.AfkStateAccessor;
import io.github.sakurawald.module.initializer.afk.config.model.AfkConfigModel;
import io.github.sakurawald.module.initializer.afk.job.AfkMarkerJob;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;


@Document("""
    This module provides:
    1. Afk detection: If a player idle too long, we will mark it as afk state.
    2. Afk event: Execute commands when a player enters or leaves afk state.
    3. Afk name customization: For a afk player, we can customize its display name in tab list.
    """)
@ColorBox("""
    How it works?

    For each player, define a `number` to track `the last action time`.
    Actions can be: `mine a block`, `movement`, `issue a command` ...
    When action received, update the number.
    Define a `job` using cron, to be triggered periodically.
    The job will check and compare 2 consecutive value of the `number`.
    If number is identical, then the player is considered as in `afk`.
    """)
public class AfkInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<AfkConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, AfkConfigModel.class);

    public static final Map<String, Long> player2prevInputCounter = new HashMap<>();

    // NOTE: Issue a command will update the lastLastActionTime, so it's impossible to use /afk to disable afk
    @CommandNode("afk")
    @Document("Enter afk state.")
    private static int $afk(@CommandSource @CommandTarget ServerPlayerEntity player) {
        if (!player.isOnGround()
            || player.isOnFire()
            || player.inPowderSnow
            || ((PlayerCombatExtension) player).fuji$inCombat()) {

            TextHelper.sendMessageByKey(player, "afk.on.failed");
            return CommandHelper.Return.FAIL;
        }

        ((AfkStateAccessor) player).fuji$changeAfk(true);
        TextHelper.sendMessageByKey(player, "afk.on");
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Test if a player is in afk state.")
    @CommandNode("test-afk")
    @CommandRequirement(level = 4)
    private static int testAfk(@CommandSource ServerCommandSource source, ServerPlayerEntity player) {
        boolean value = isAfk(player);
        return CommandHelper.Return.outputBoolean(source, value);
    }

    public static boolean isAfk(Entity entity) {
        if (entity instanceof ServerPlayerEntity) {
            AfkStateAccessor afkStateAccessor = (AfkStateAccessor) entity;
            return afkStateAccessor.fuji$isAfk();
        }
        return false;
    }

    public static void countAction(ServerPlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            AfkStateAccessor ex = (AfkStateAccessor) player;
            ex.fuji$incrInputCounter();
        }
    }

    public static Text getAfkText(ServerPlayerEntity player) {
        return TextHelper.getTextByValue(player, AfkInitializer.config.model().format);
    }

    public static boolean isPlayerVelocityNotZero(MovementType movementType, Vec3d vec3d) {
        // if a player itself moved.
        if (movementType == MovementType.PLAYER) {
            // filter zero movement: Vec3d.ZERO
            return Double.compare(vec3d.x, 0) != 0
                || Double.compare(vec3d.y, 0) != 0
                || Double.compare(vec3d.z, 0) != 0;
        }

        return false;
    }

    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> new AfkMarkerJob().schedule());
    }

}
