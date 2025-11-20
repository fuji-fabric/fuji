package mod.fuji.core.structure;

import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalBlockPos {
    String dimension;
    int x;
    int y;
    int z;

    public GlobalBlockPos(Level world, BlockPos blockPos) {
        this.dimension = RegistryHelper.getIdAsString(world);
        this.x = blockPos.getX();
        this.y = blockPos.getY();
        this.z = blockPos.getZ();
    }

    public @NotNull BlockPos toBlockPos() {
        return new BlockPos(this.x, this.y, this.z);
    }
}
