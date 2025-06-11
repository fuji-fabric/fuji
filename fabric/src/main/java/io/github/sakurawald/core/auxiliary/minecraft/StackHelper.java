package io.github.sakurawald.core.auxiliary.minecraft;

import io.github.sakurawald.core.auxiliary.LogUtil;
import lombok.experimental.UtilityClass;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
#if MC_VER <= MC_1_20_4
#elif MC_VER > MC_1_20_4
import net.minecraft.component.DataComponentTypes;
#endif

import net.minecraft.text.Text;

import java.util.Optional;

@UtilityClass
public class StackHelper {

    public NbtElement toNbt(ItemStack stack, RegistryWrapper.WrapperLookup wrapperLookup, NbtElement nbtElement) {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Cannot encode empty ItemStack");
        }
        return ItemStack.CODEC
            .encode(stack, wrapperLookup.getOps(NbtOps.INSTANCE), nbtElement)
            .getOrThrow();
    }

    public static Optional<ItemStack> fromNbt(RegistryWrapper.WrapperLookup wrapperLookup, NbtElement nbtElement) {
        return ItemStack.CODEC
            .parse(wrapperLookup.getOps(NbtOps.INSTANCE), nbtElement)
            .resultOrPartial(string -> LogUtil.error("Tried to load invalid item: '{}'", string));
    }

    public static void setCustomName(ItemStack stack, Text customName) {
        #if MC_VER <= MC_1_20_4
            stack.setCustomName(customName);
        #elif MC_VER > MC_1_20_4
            stack.set(DataComponentTypes.CUSTOM_NAME, customName);
        #endif
    }
}
