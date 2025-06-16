package io.github.sakurawald.module.mixin.gameplay.multi_obsidian_platform;

#if MC_VER <= MC_1_20_6
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.sakurawald.module.initializer.gameplay.multi_obsidian_platform.MultiObsidianPlatformInitializer;
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
    BlockPos getTransformedEndSpawnPoint() {
        Entity entity = (Entity) (Object) this;
        return MultiObsidianPlatformInitializer.transform(entity.getBlockPos());
    }

    @Unique
    World getEntityCurrentLevel() {
        Entity entity = (Entity) (Object) this;
        return entity.getWorld();
    }

    @ModifyExpressionValue(method = "getTeleportTarget", at = @At(value = "FIELD", target = "Lnet/minecraft/server/world/ServerWorld;END_SPAWN_POS:Lnet/minecraft/util/math/BlockPos;"), require = 1)
    BlockPos $findDimensionEntryPoint(BlockPos original) {
        // modify: resource_world:overworld -> minecraft:the_end (default obsidian platform)
        // feature: https://bugs.mojang.com/browse/MC-252361
        if (getEntityCurrentLevel().getRegistryKey() != World.OVERWORLD) return ServerWorld.END_SPAWN_POS;
        return getTransformedEndSpawnPoint();
    }

    @Unique
    public void makeObsidianPlatform(ServerWorld serverLevel, BlockPos centerBlockPos) {
        int i = centerBlockPos.getX();
        int j = centerBlockPos.getY() - 2;
        int k = centerBlockPos.getZ();
        BlockPos.iterate(i - 2, j + 1, k - 2, i + 2, j + 3, k + 2).forEach(blockPos -> serverLevel.setBlockState(blockPos, Blocks.AIR.getDefaultState()));
        BlockPos.iterate(i - 2, j, k - 2, i + 2, j, k + 2).forEach(blockPos -> serverLevel.setBlockState(blockPos, Blocks.OBSIDIAN.getDefaultState()));
    }

    /*
    1. In vanilla Minecraft, when player entity and non-player entity jump into the Ender Portal Frame, the obsidian platform will be re-created.
    2. For ServerPlayerEntity, the obsidian platform location is the player's teleport location.
    3. For Entity, the obsidian platform location is the fixed location END_SPAWN_POS.

    This method will NOT be called when a PLAYER jump into overworld's ender-portal-frame */
    @WrapOperation(method = "moveToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;createEndSpawnPlatform(Lnet/minecraft/server/world/ServerWorld;)V"), require = 0)
    // NOTE: require = 0, due to mixin fails in forge platform.
    private void makeObsidianPlatformWhenNonPlayerEntityPassThroughThePortal(ServerWorld toLevel, Operation<Void> original) {
        // modify: resource_world:overworld -> minecraft:the_end (default obsidian platform)
        if (getEntityCurrentLevel().getRegistryKey() != World.OVERWORLD) {
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

