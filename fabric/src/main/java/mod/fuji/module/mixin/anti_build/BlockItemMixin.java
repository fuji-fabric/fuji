package mod.fuji.module.mixin.anti_build;

import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.module.initializer.anti_build.AntiBuildInitializer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(method = "canPlace", at = @At("RETURN"), cancellable = true)
    void handlePlaceBlock(@NotNull ItemPlacementContext itemPlacementContext, BlockState blockState, @NotNull CallbackInfoReturnable<Boolean> cir) {
        var config = AntiBuildInitializer.config.model().getAntiTypes().getPlaceBlock();
        if (!config.isEnable()) return;

        @Nullable PlayerEntity player = itemPlacementContext.getPlayer();
        String id = RegistryHelper.getIdAsString(itemPlacementContext.getStack());
        Hand hand = itemPlacementContext.getHand();

        AntiBuildInitializer.processAntiBuild(player, "place_block", config.getId(), id, cir, false, () -> hand == Hand.MAIN_HAND);
    }
}
