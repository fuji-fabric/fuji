package io.github.sakurawald.fuji.module.mixin.anti_build;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.module.initializer.anti_build.AntiBuildInitializer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(method = "canPlace", at = @At("RETURN"), cancellable = true)
    void handlePlaceBlock(@NotNull ItemPlacementContext itemPlacementContext, BlockState blockState, @NotNull CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = itemPlacementContext.getPlayer();
        // NOTE: The canPlace() function will be called twice, one in client-side, one in server-side. (One ClientPlayerEntity and one ServerPlayerEntity)
        if (player != null && !PlayerHelper.isServerPlayer(player)) {
            return;
        }

        String id = RegistryHelper.getIdAsString(itemPlacementContext.getStack());
        Hand hand = itemPlacementContext.getHand();

        AntiBuildInitializer.checkAntiBuild(player, "place_block", AntiBuildInitializer.config.model().anti.place_block.id, id, cir, false, () -> hand == Hand.MAIN_HAND);
    }
}
