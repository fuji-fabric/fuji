package mod.fuji.module.mixin.gameplay.multi_obsidian_platform;

#if MC_VER <= MC_1_20_6
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.sakurawald.fuji.module.initializer.gameplay.multi_obsidian_platform.MultiObsidianPlatformInitializer;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Unique
    World getEntityCurrentDimension() {
        Entity entity = (Entity) (Object) this;
        return entity.getWorld();
    }

    @Unique
    BlockPos getTransformedEndSpawnPoint() {
        Entity entity = (Entity) (Object) this;
        return MultiObsidianPlatformInitializer.getTransformedEndSpawnPosition(entity.getBlockPos());
    }

    @Unique
    public void makeObsidianPlatform(ServerWorld serverLevel, BlockPos centerBlockPos) {
        int i = centerBlockPos.getX();
        int j = centerBlockPos.getY() - 2;
        int k = centerBlockPos.getZ();
        BlockPos.iterate(i - 2, j + 1, k - 2, i + 2, j + 3, k + 2).forEach(blockPos -> serverLevel.setBlockState(blockPos, Blocks.AIR.getDefaultState()));
        BlockPos.iterate(i - 2, j, k - 2, i + 2, j, k + 2).forEach(blockPos -> serverLevel.setBlockState(blockPos, Blocks.OBSIDIAN.getDefaultState()));
    }

    @ModifyExpressionValue(method = "getTeleportTarget", at = @At(value = "FIELD", target = "Lnet/minecraft/server/world/ServerWorld;END_SPAWN_POS:Lnet/minecraft/util/math/BlockPos;"), require = 1)
    BlockPos onAnyEntityPassThroughAnEndPortal(BlockPos original) {
        // NOTE: When pass through a portal in fuji:overworld, you will be spawned in minecraft:the_end at (100, 50, 0)
        if (getEntityCurrentDimension().getRegistryKey() != World.OVERWORLD) {
            return ServerWorld.END_SPAWN_POS;
        }

        return getTransformedEndSpawnPoint();
    }

    @WrapOperation(method = "moveToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;createEndSpawnPlatform(Lnet/minecraft/server/world/ServerWorld;)V"), require = 0)
    // NOTE: require = 0, due to mixin failure in forge platform.
    private void onNonPlayerEntityPassThroughAnEndPortal(ServerWorld toLevel, Operation<Void> original) {
        // NOTE: This method only modifies the Entity#moveToWorld method, that's because for non-player-entity, the obsidian platform position is computed by END_SPAWN_POS field.
        // However, for ServerPlayerEntity, the ServerPlayerEntity#moveToWorld method will compute the obsidian platform position.

        if (getEntityCurrentDimension().getRegistryKey() != World.OVERWORLD) {
            ServerWorld.createEndSpawnPlatform(toLevel);
            return;
        }
        makeObsidianPlatform(toLevel, getTransformedEndSpawnPoint());
    }

}
#elif MC_VER > MC_1_20_6

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(Entity.class)
public abstract class EntityMixin {

}
#endif

