package io.github.sakurawald.module.mixin.gameplay.multi_obsidian_platform;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.sakurawald.module.initializer.gameplay.multi_obsidian_platform.MultiObsidianPlatformInitializer;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {

    @Unique
    BlockPos getTransformedEndSpawnPoint(@NotNull Entity entity) {
        return MultiObsidianPlatformInitializer.transform(entity.getBlockPos());
    }

    @Unique
    World getEntityCurrentLevel(@NotNull Entity entity) {
        return entity.getWorld();
    }

    @Redirect(method = "createTeleportTarget", at = @At(value = "FIELD", target = "Lnet/minecraft/server/world/ServerWorld;END_SPAWN_POS:Lnet/minecraft/util/math/BlockPos;")
    )
        /* This method will NOT be called when an entity (including player, item and other entities) jump into overworld's ender-portal-frame */
    BlockPos modifyTheEndSpawnPosConstant(@Local(argsOnly = true) @NotNull Entity entity) {
        if (getEntityCurrentLevel(entity).getRegistryKey() != World.OVERWORLD) {
            // modify: world:overworld -> minecraft:the_end (default obsidian platform)
            // feature: https://bugs.mojang.com/browse/MC-252361
            return ServerWorld.END_SPAWN_POS;
        }
        return getTransformedEndSpawnPoint(entity);
    }

}
