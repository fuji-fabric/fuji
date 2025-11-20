package mod.fuji.module.mixin.anti_build;

import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.module.initializer.anti_build.AntiBuildInitializer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(method = "canPlace", at = @At("RETURN"), cancellable = true)
    void handlePlaceBlock(@NotNull BlockPlaceContext itemPlacementContext, BlockState blockState, @NotNull CallbackInfoReturnable<Boolean> cir) {
        var config = AntiBuildInitializer.config.model().getAntiTypes().getPlaceBlock();
        if (!config.isEnable()) return;

        @Nullable Player player = itemPlacementContext.getPlayer();
        String id = RegistryHelper.getIdAsString(itemPlacementContext.getItemInHand());
        InteractionHand hand = itemPlacementContext.getHand();

        AntiBuildInitializer.processAntiBuild(player, "place_block", config.getId(), id, cir, false, () -> hand == InteractionHand.MAIN_HAND);
    }
}
