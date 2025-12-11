package mod.fuji.module.mixin.works;

import com.llamalad7.mixinextras.sugar.Local;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.module.initializer.works.structure.WorksBinding;
import mod.fuji.module.initializer.works.structure.work.abst.Work;
import mod.fuji.module.initializer.works.structure.work.impl.ProductionWork;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

// NOTE: The priority of carpet's is 1000. We take priority over carpet, to prevent items from being destroyed. So, we are compatible with carpet mod.
@Mixin(value = HopperBlockEntity.class, priority = 1000 - 1)

public abstract class HopperBlockEntityMixin extends RandomizableContainerBlockEntity {

    protected HopperBlockEntityMixin(BlockEntityType<?> blockEntityType, @NotNull BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "tryMoveInItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.AFTER))
    private static void $ifHopperHasEmptySlot(Container container, Container container2, @NotNull ItemStack itemStack, int i, Direction direction, CallbackInfoReturnable<ItemStack> cir) {
        countItemStack(container, container2, itemStack);
    }


    @Inject(method = "tryMoveInItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;grow(I)V", shift = At.Shift.AFTER))
    private static void $ifHopperHasMergableSlot(Container container, Container container2, @NotNull ItemStack itemStack, int i, Direction direction, CallbackInfoReturnable<ItemStack> cir, @Local(ordinal = 2) int k) {
        /* We injected in the increment() instead of decrement().
         If the count of itemStack is shark to 0, then it may become AIR, and then we can't count it any more.
        */
        ItemStack copy = itemStack.copy();
        copy.setCount(k);

        countItemStack(container, container2, copy);
    }

    @Unique
    private static void countItemStack(@Nullable Container sourceContainer, Container destinationContainer, @NotNull ItemStack itemStack) {
        // NOTE: if the container == null, then means it's the source-hopper. We only count the source-hopper.
        if (sourceContainer != null) return;
        if (itemStack.isEmpty()) return;

        /* Find the bound works to destination container. */
        Set<Work> boundWorks;
        if (destinationContainer instanceof HopperBlockEntity hb) {
            boundWorks = WorksBinding.BLOCK_POS_2_WORKS.get(hb.getBlockPos());
        } else if (destinationContainer instanceof
            #if MC_VER < MC_1_21_11
            net.minecraft.world.entity.vehicle.MinecartHopper
            #elif MC_VER >= MC_1_21_11
            net.minecraft.world.entity.vehicle.minecart.MinecartHopper
            #endif mh) {
            boundWorks = WorksBinding.ENTITY_2_WORKS.get(mh.getId());
        } else {
            LogUtil.warn("Unknown container type: {} (It should be HopperBlockEntity or HopperMinecartEntity)", destinationContainer);
            return;
        }
        if (boundWorks == null) return;

        /* Count this itemstack for all works that contain this blockpos */
        boundWorks.forEach(work -> {
            if (work instanceof ProductionWork pwork) {
                pwork.addCounter(itemStack);
            }
        });
    }

}
