package mod.fuji.module.mixin.command_event;

import mod.fuji.module.initializer.command_event.CommandEventInitializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
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

    @Inject(method = "setPlacedBy", at = @At("RETURN"))
    void onBlockPlaced(Level world, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack, CallbackInfo ci) {
        var config = CommandEventInitializer.config.model().getEvent().getAfterPlayerPlaceBlock();
        if (config.isEnable()) {
            if (livingEntity instanceof ServerPlayer player) {
                CommandEventInitializer.executeCommandOnEvent(player, config.getCommands());
            }
        }
    }

    @Inject(method = "playerWillDestroy", at = @At("RETURN"))
    #if MC_VER <= MC_1_20_2
    void onBlockBreak(World world, BlockPos blockPos, BlockState blockState, PlayerEntity playerEntity, CallbackInfo ci)
    #elif MC_VER > MC_1_20_2
    void onBlockBreak(Level world, BlockPos blockPos, BlockState blockState, Player playerEntity, CallbackInfoReturnable<BlockState> cir)
    #endif
    {
        var config = CommandEventInitializer.config.model().getEvent().getAfterPlayerBreakBlock();
        if (config.isEnable()) {
            if (playerEntity instanceof ServerPlayer player) {
                CommandEventInitializer.executeCommandOnEvent(player, config.getCommands());
            }
        }
    }

}
