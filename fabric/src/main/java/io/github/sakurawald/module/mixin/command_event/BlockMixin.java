package io.github.sakurawald.module.mixin.command_event;

import io.github.sakurawald.core.command.executor.CommandExecutor;
import io.github.sakurawald.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.module.initializer.command_event.CommandEventInitializer;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "onPlaced", at = @At("TAIL"))
    void onBlockPlaced(World world, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack, CallbackInfo ci) {
        if (livingEntity instanceof ServerPlayerEntity player) {
            CommandExecutor.execute(ExtendedCommandSource.asConsole(player.getCommandSource()), CommandEventInitializer.config.model().event.after_player_place_block.command_list);
        }
    }

    @Inject(method = "onBreak", at = @At("TAIL"))
    void onBlockBreak(World world, BlockPos blockPos, BlockState blockState, PlayerEntity playerEntity, CallbackInfoReturnable<BlockState> cir) {
        if (playerEntity instanceof ServerPlayerEntity player) {
            CommandExecutor.execute(ExtendedCommandSource.asConsole(player.getCommandSource()), CommandEventInitializer.config.model().event.after_player_break_block.command_list);
        }
    }

}
