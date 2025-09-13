package io.github.sakurawald.fuji.core.service.random_teleport;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.service.random_teleport.structure.RandomTeleportSettings;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class LocationFilter {

    public static final Set<Block> KNOWN_DANGEROUS_BLOCKS = Set.of(
        Blocks.POWDER_SNOW,
        Blocks.MAGMA_BLOCK,
        Blocks.FIRE,
        Blocks.SWEET_BERRY_BUSH,
        Blocks.CACTUS,
        Blocks.VOID_AIR,
        Blocks.CAMPFIRE
    );

    public static boolean isYInRange(@NotNull RandomTeleportSettings settings, @NotNull BlockPos blockPos) {
        return blockPos.getY() >= settings.getMinY()
            && blockPos.getY() <= settings.getMaxY();
    }

    public static boolean isInsideWorldBorder(@NotNull ServerWorld world, @NotNull BlockPos blockPos) {
        return world.getWorldBorder().contains(blockPos);
    }

    public static boolean isSafeBlock(@NotNull RandomTeleportSettings settings, @NotNull BlockState blockState) {
        String blockId = RegistryHelper.getIdAsString(blockState);
        return isSafeBlock(blockState)
            && !settings.getBlocks().getSkip().contains(blockId);
    }

    public static boolean isSafeBlock(@NotNull BlockState blockState) {
        return !isLiquidBlock(blockState)
            && !KNOWN_DANGEROUS_BLOCKS.contains(blockState.getBlock());
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isLiquidBlock(@NotNull BlockState blockState) {
        return !blockState.getFluidState().isEmpty();
    }
}
