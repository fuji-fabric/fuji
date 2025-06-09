package io.github.sakurawald.core.structure;

import io.github.sakurawald.core.auxiliary.minecraft.RegistryHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
public class SpatialBlock {
    String dimension;
    int x;
    int y;
    int z;

    public SpatialBlock(World world, BlockPos blockPos) {
        this.dimension = RegistryHelper.ofString(world);
        this.x = blockPos.getX();
        this.y = blockPos.getY();
        this.z = blockPos.getZ();
    }

    public ServerWorld ofDimension() {
        return RegistryHelper.ofServerWorld(this.dimension);
    }

    public @NotNull BlockPos ofBlockPos() {
        return new BlockPos(this.x, this.y, this.z);
    }
}
