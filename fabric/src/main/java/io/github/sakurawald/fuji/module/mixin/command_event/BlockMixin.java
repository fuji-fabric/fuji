package io.github.sakurawald.fuji.module.mixin.command_event;

import io.github.sakurawald.fuji.module.initializer.command_event.CommandEventInitializer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

#if MC_VER <= MC_1_20_2
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
#elif MC_VER > MC_1_20_2
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
#endif


@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "onPlaced", at = @At("TAIL"))
    void onBlockPlaced(World world, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack, CallbackInfo ci) {
        var config = CommandEventInitializer.config.model().getEvent().getAfterPlayerPlaceBlock();
        if (config.isEnable()) {
            if (livingEntity instanceof ServerPlayerEntity player) {
                CommandEventInitializer.executeCommandOnEvent(player, config.getCommands());
            }
        }
    }

    @Inject(method = "onBreak", at = @At("TAIL"))
    #if MC_VER <= MC_1_20_2
    void onBlockBreak(World world, BlockPos blockPos, BlockState blockState, PlayerEntity playerEntity, CallbackInfo ci)
    #elif MC_VER > MC_1_20_2
    void onBlockBreak(World world, BlockPos blockPos, BlockState blockState, PlayerEntity playerEntity, CallbackInfoReturnable<BlockState> cir)
    #endif
    {
        var config = CommandEventInitializer.config.model().getEvent().getAfterPlayerBreakBlock();
        if (config.isEnable()) {
            if (playerEntity instanceof ServerPlayerEntity player) {
                CommandEventInitializer.executeCommandOnEvent(player, config.getCommands());
            }
        }
    }

}
