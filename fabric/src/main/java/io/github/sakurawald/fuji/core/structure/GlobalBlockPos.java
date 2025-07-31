package io.github.sakurawald.fuji.core.structure;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
public class GlobalBlockPos {
    String dimension;
    int x;
    int y;
    int z;

    public GlobalBlockPos(World world, BlockPos blockPos) {
        this.dimension = RegistryHelper.toString(world);
        this.x = blockPos.getX();
        this.y = blockPos.getY();
        this.z = blockPos.getZ();
    }

    public ServerWorld toDimension() {
        return RegistryHelper.getServerWorld(this.dimension);
    }

    public @NotNull BlockPos toBlockPos() {
        return new BlockPos(this.x, this.y, this.z);
    }
}
