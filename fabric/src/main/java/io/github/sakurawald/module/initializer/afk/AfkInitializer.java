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
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.afk.accessor.AfkStateAccessor;
import io.github.sakurawald.module.initializer.afk.config.model.AfkConfigModel;
import io.github.sakurawald.module.initializer.afk.job.AfkMarkerJob;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;


public class AfkInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<AfkConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, AfkConfigModel.class);

    public static final Map<String, Long> player2prevInputCounter = new HashMap<>();

    // note: issue command will update lastLastActionTime, so it's impossible to use /afk to disable afk
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

    @CommandNode("test-afk")
    @CommandRequirement(level = 4)
    private static int testAfk(@CommandSource ServerCommandSource source, ServerPlayerEntity player) {
        boolean value = ((AfkStateAccessor) player).fuji$isAfk();
        return CommandHelper.Return.outputBoolean(source, value);
    }

    public static boolean isAfk(Entity entity) {
        if (entity instanceof ServerPlayerEntity) {
            AfkStateAccessor afkStateAccessor = (AfkStateAccessor) entity;
            return afkStateAccessor.fuji$isAfk();
        }
        return false;
    }

    public static boolean isPlayerActuallyMovedItself(MovementType movementType, Vec3d vec3d) {
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
