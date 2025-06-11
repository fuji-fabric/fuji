package io.github.sakurawald.core.auxiliary.minecraft;

import io.github.sakurawald.core.auxiliary.LogUtil;
import lombok.experimental.UtilityClass;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@UtilityClass
public class StackHelper {

    public static final String LORE_NBT_KEY = "Lore";
    public static final String DISPLAY_NBT_KEY = "display";

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

    public static boolean hasCustomName(ItemStack stack) {
        #if MC_VER <= MC_1_20_4
        return stack.hasCustomName();
        #elif MC_VER > MC_1_20_4
            return stack.get(DataComponentTypes.CUSTOM_NAME) != null;
        #endif
    }

    public static List<Text> getLore(ItemStack stack) {
        return stack
            .getOrCreateSubNbt("display")
            .getList("Lore", NbtElement.STRING_TYPE)
            .stream()
            .map(tag -> Text.Serialization.fromJson(tag.asString())).collect(Collectors.toList());
    }

    public static void setLore(ItemStack stack, List<Text> texts) {
        #if MC_VER <= MC_1_20_4
        NbtCompound display = stack.getOrCreateSubNbt("display");
        NbtList loreItems = new NbtList();
        for (Text text : texts) {
            loreItems.add(NbtString.of(Text.Serialization.toJsonString(text)));
        }
        display.put("Lore", loreItems);
        #elif MC_VER > MC_1_20_4
            LoreComponent loreComponent = new LoreComponent(texts);
            stack.set(DataComponentTypes.LORE, loreComponent);
        #endif
    }

    public static NbtCompound getSkullOwner(ItemStack stack) {
        return stack.getSubNbt("SkullOwner");
    }

    public static boolean canCombine(ItemStack itemStack, ItemStack itemStack2) {
        if (!itemStack.isOf(itemStack2.getItem())) {
            return false;
        }
        if (itemStack.isEmpty() && itemStack2.isEmpty()) {
            return true;
        }
        return Objects.equals(NbtHelper.getNbt(itemStack), NbtHelper.getNbt(itemStack2));
    }

}
