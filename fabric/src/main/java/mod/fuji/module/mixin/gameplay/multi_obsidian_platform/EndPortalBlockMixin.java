package mod.fuji.module.mixin.gameplay.multi_obsidian_platform;

#if MC_VER <= MC_1_20_6
import net.minecraft.block.EndPortalBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {

}

#elif MC_VER > MC_1_20_6
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.module.initializer.gameplay.multi_obsidian_platform.MultiObsidianPlatformInitializer;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {

    @Unique
    BlockPos getTransformedEndSpawnPoint(@NotNull Entity entity) {
        return MultiObsidianPlatformInitializer.getTransformedEndSpawnPosition(entity.getBlockPos());
    }

    @Unique
    World getEntityCurrentDimension(@NotNull Entity entity) {
        return EntityHelper.getServerWorld(entity);
    }

    @WrapOperation(method = "createTeleportTarget", at = @At(value = "FIELD", target = "Lnet/minecraft/server/world/ServerWorld;END_SPAWN_POS:Lnet/minecraft/util/math/BlockPos;"))
    BlockPos modifyTheEndSpawnPosConstant(Operation<BlockPos> original, @Local(argsOnly = true) @NotNull Entity entity) {
        // NOTE: This method will be called when an ENTITY (including PLAYER, ITEM and other types of entities) pass through an END_PORTAL_BLOCK. (Unless the block is already in minecraft:the_end)
        if (getEntityCurrentDimension(entity).getRegistryKey() != World.OVERWORLD) {
            // NOTE: If you jump into the EnderPortal in fuji:overworld, then you will be spawned in the minecraft:the_end at (100, 50, 0)
            return ServerWorld.END_SPAWN_POS;
        }
        return getTransformedEndSpawnPoint(entity);
    }
}
#endif

