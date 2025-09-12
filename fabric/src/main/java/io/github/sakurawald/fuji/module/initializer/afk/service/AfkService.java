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
import org.jetbrains.annotations.NotNull;

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

    public static void countAction(@NotNull ServerPlayerEntity player) {
        AfkStateAccessor playerEx = (AfkStateAccessor) player;
        playerEx.fuji$incrInputCounter();
    }

    public static @NotNull Text getAfkText(@NotNull ServerPlayerEntity player) {
        return TextHelper.getTextByValue(player, AfkInitializer.config.model().afk_display_name_format);
    }

    public static boolean isPlayerMovedBySelf(MovementType movementType, Vec3d vec3d) {
        if (movementType == MovementType.PLAYER) {
            // NOTE: In Minecraft's protocol, the client will send the velocity update packet even for (0, 0, 0)
            return Double.compare(vec3d.x, 0) != 0
                || Double.compare(vec3d.y, 0) != 0
                || Double.compare(vec3d.z, 0) != 0;
        }

        return false;
    }
}
