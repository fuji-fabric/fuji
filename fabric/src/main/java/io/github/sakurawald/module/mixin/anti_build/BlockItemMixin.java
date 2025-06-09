package io.github.sakurawald.module.mixin.anti_build;

import io.github.sakurawald.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.module.initializer.anti_build.AntiBuildInitializer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(method = "canPlace", at = @At("RETURN"), cancellable = true)
    public void $canPlace(@NotNull ItemPlacementContext itemPlacementContext, BlockState blockState, @NotNull CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = itemPlacementContext.getPlayer();
        String id = RegistryHelper.ofString(itemPlacementContext.getStack());

        AntiBuildInitializer.checkAntiBuild(player, "place_block", AntiBuildInitializer.config.model().anti.place_block.id, id, cir, false, () -> true);
    }
}
