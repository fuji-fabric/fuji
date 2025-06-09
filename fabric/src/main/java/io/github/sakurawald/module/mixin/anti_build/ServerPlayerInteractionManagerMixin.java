package io.github.sakurawald.module.mixin.anti_build;

import io.github.sakurawald.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.module.initializer.anti_build.AntiBuildInitializer;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow
    protected ServerWorld world;

    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
    void $tryBreak(BlockPos blockPos, @NotNull CallbackInfoReturnable<Boolean> cir) {
        BlockState blockState = this.world.getBlockState(blockPos);
        String id = RegistryHelper.ofString(blockState);

        AntiBuildInitializer.checkAntiBuild(player, "break_block", AntiBuildInitializer.config.model().anti.break_block.id, id, cir, false, () -> true);
    }

    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    void $interactItem(ServerPlayerEntity serverPlayerEntity, World world, @NotNull ItemStack itemStack, Hand hand, @NotNull CallbackInfoReturnable<ActionResult> cir) {
        String id = RegistryHelper.ofString(itemStack);

        AntiBuildInitializer.checkAntiBuild(player, "interact_item", AntiBuildInitializer.config.model().anti.interact_item.id, id, cir, ActionResult.FAIL, () -> true);
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    void $interactBlock(ServerPlayerEntity serverPlayerEntity, @NotNull World world, ItemStack itemStack, Hand hand, @NotNull BlockHitResult blockHitResult, @NotNull CallbackInfoReturnable<ActionResult> cir) {
        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        String id = RegistryHelper.ofString(blockState);

        AntiBuildInitializer.checkAntiBuild(player, "interact_block", AntiBuildInitializer.config.model().anti.interact_block.id, id, cir, ActionResult.FAIL, () -> true);
    }

}
