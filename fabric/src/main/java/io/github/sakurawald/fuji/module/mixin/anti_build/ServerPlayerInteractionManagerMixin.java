package io.github.sakurawald.fuji.module.mixin.anti_build;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.module.initializer.anti_build.AntiBuildInitializer;
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

    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    void handleInteractItem(ServerPlayerEntity serverPlayerEntity, World world, @NotNull ItemStack itemStack, Hand hand, @NotNull CallbackInfoReturnable<ActionResult> cir) {
        var config = AntiBuildInitializer.config.model().getAnti().getInteractItem();
        if (!config.isEnable()) return;

        String id = RegistryHelper.getIdAsString(itemStack);

        AntiBuildInitializer.processAntiBuild(player, "interact_item", config.getId(), id, cir, ActionResult.FAIL, () -> true);
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    void handleInteractBlock(ServerPlayerEntity serverPlayerEntity, @NotNull World world, ItemStack itemStack, Hand hand, @NotNull BlockHitResult blockHitResult, @NotNull CallbackInfoReturnable<ActionResult> cir) {
        var config = AntiBuildInitializer.config.model().getAnti().getInteractBlock();
        if (!config.isEnable()) return;

        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        String id = RegistryHelper.getIdAsString(blockState);

        AntiBuildInitializer.processAntiBuild(player, "interact_block", config.getId(), id, cir, ActionResult.FAIL, () -> true);
    }

}
