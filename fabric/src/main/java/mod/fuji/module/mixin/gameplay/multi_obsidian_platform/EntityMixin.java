package mod.fuji.module.mixin.gameplay.multi_obsidian_platform;

#if MC_VER <= MC_1_20_6
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mod.fuji.module.initializer.gameplay.multi_obsidian_platform.MultiObsidianPlatformInitializer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Unique
    Level getEntityCurrentDimension() {
        Entity entity = (Entity) (Object) this;
        return entity.level();
    }

    @Unique
    BlockPos getTransformedEndSpawnPoint() {
        Entity entity = (Entity) (Object) this;
        return MultiObsidianPlatformInitializer.getTransformedEndSpawnPosition(entity.blockPosition());
    }

    @Unique
    public void makeObsidianPlatform(ServerLevel serverLevel, BlockPos centerBlockPos) {
        int i = centerBlockPos.getX();
        int j = centerBlockPos.getY() - 2;
        int k = centerBlockPos.getZ();
        BlockPos.betweenClosed(i - 2, j + 1, k - 2, i + 2, j + 3, k + 2).forEach(blockPos -> serverLevel.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState()));
        BlockPos.betweenClosed(i - 2, j, k - 2, i + 2, j, k + 2).forEach(blockPos -> serverLevel.setBlockAndUpdate(blockPos, Blocks.OBSIDIAN.defaultBlockState()));
    }

    @ModifyExpressionValue(method = "findDimensionEntryPoint", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerLevel;END_SPAWN_POINT:Lnet/minecraft/core/BlockPos;", opcode = Opcodes.GETSTATIC), require = 1)
    BlockPos onAnyEntityPassThroughAnEndPortal(BlockPos original) {
        // NOTE: When pass through a portal in fuji:overworld, you will be spawned in minecraft:the_end at (100, 50, 0)
        if (getEntityCurrentDimension().dimension() != Level.OVERWORLD) {
            return ServerLevel.END_SPAWN_POINT;
        }

        return getTransformedEndSpawnPoint();
    }

    @WrapOperation(method = "changeDimension", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;makeObsidianPlatform(Lnet/minecraft/server/level/ServerLevel;)V"), require = 0)
    // NOTE: require = 0, due to mixin failure in forge platform.
    private void onNonPlayerEntityPassThroughAnEndPortal(ServerLevel toLevel, Operation<Void> original) {
        // NOTE: This method only modifies the Entity#moveToWorld method, that's because for non-player-entity, the obsidian platform position is computed by END_SPAWN_POS field.
        // However, for ServerPlayerEntity, the ServerPlayerEntity#moveToWorld method will compute the obsidian platform position.

        if (getEntityCurrentDimension().dimension() != Level.OVERWORLD) {
            ServerLevel.makeObsidianPlatform(toLevel);
            return;
        }
        makeObsidianPlatform(toLevel, getTransformedEndSpawnPoint());
    }

}
#elif MC_VER > MC_1_20_6
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(Entity.class)
public abstract class EntityMixin {

}
#endif

