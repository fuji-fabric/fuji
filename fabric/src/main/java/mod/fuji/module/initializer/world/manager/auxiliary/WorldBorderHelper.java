package mod.fuji.module.initializer.world.manager.auxiliary;

import net.minecraft.world.level.border.WorldBorder;
import org.jetbrains.annotations.NotNull;

public class WorldBorderHelper {

    public static double getLerpTime(@NotNull WorldBorder worldBorder) {
        #if MC_VER < MC_1_21_9
        return worldBorder.getLerpRemainingTime();
        #elif MC_VER >= MC_1_21_9
        return worldBorder.getLerpTime();
        #endif
    }

    public static double getSafeZone(@NotNull WorldBorder worldBorder) {
        #if MC_VER < MC_1_21_9
        return worldBorder.getDamageSafeZone();
        #elif MC_VER >= MC_1_21_9
        return worldBorder.getSafeZone();
        #endif
    }

}
