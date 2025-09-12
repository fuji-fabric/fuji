package io.github.sakurawald.fuji.module.initializer.afk.service;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.player.ModifyPlayerListNameEvent;
import io.github.sakurawald.fuji.module.initializer.afk.AfkInitializer;
import io.github.sakurawald.fuji.module.initializer.afk.accessor.AfkStateAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class AfkService {

    @TestCase(action = "Issue `/afk` and see the player list.", targets = "The display name of an afk player should be modified.")
    @EventConsumer(injectorPriority = EventConsumer.HIGHEST, consumerPriority = EventConsumer.HIGHEST)
    private static void modifyPlayerListName(ModifyPlayerListNameEvent event) {
        ServerPlayerEntity player = event.getPlayer();
        if (isAfk(player)) {
            Text newValue = getAfkText(player);
            event.setText(newValue);
        }
    }

    public static boolean isAfk(Entity entity) {
        if (entity instanceof ServerPlayerEntity) {
            AfkStateAccessor afkStateAccessor = (AfkStateAccessor) entity;
            return afkStateAccessor.fuji$isAfk();
        }
        return false;
    }

    public static void countAction(ServerPlayerEntity player) {
        AfkStateAccessor ex = (AfkStateAccessor) player;
        ex.fuji$incrInputCounter();
    }

    public static Text getAfkText(ServerPlayerEntity player) {
        return TextHelper.getTextByValue(player, AfkInitializer.config.model().afk_display_name_format);
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
}
