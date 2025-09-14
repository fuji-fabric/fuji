package io.github.sakurawald.fuji.core.service.random_teleport;

import io.github.sakurawald.fuji.core.service.random_teleport.structure.RandomTeleportSettings;
import java.util.Random;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class PositionXZGenerator {

    public static @NotNull BlockPos getRandomXZ(@NotNull RandomTeleportSettings settings) {
        return settings.isCircle() ? getRandomXZWithCircle(settings) : getRandomXZWithRect(settings);
    }

    private static @NotNull BlockPos getRandomXZWithCircle(@NotNull RandomTeleportSettings settings) {
        var rand = new Random();

        int r_min = settings.getMinRange();
        int r_max = settings.getMaxRange();
        int r = r_max == r_min
            ? r_max
            : rand.nextInt(r_min, r_max);
        final double angle = rand.nextDouble() * 2 * Math.PI;
        final double delta_x = r * Math.cos(angle);
        final double delta_z = r * Math.sin(angle);
        int x = settings.getCenterX() + (int) delta_x;
        int z = settings.getCenterZ() + (int) delta_z;
        return new BlockPos(x, 0, z);
    }

    private static @NotNull BlockPos getRandomXZWithRect(@NotNull RandomTeleportSettings settings) {
        var rand = new Random();
        int r_min = settings.getMinRange();
        int r_max = settings.getMaxRange();

        int x = settings.getCenterX() + rand.nextInt(r_min, r_max);
        int z = settings.getCenterZ() + rand.nextInt(r_min, r_max);
        return new BlockPos(x, 0, z);
    }
}
