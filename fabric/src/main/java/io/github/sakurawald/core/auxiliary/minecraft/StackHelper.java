package io.github.sakurawald.core.auxiliary.minecraft;

import io.github.sakurawald.core.auxiliary.LogUtil;
import lombok.experimental.UtilityClass;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
#if MC_VER <= MC_1_20_4
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;
#elif MC_VER > MC_1_20_4
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
#endif


@UtilityClass
public class StackHelper {

    public static final String LORE_NBT_KEY = "Lore";
    public static final String DISPLAY_NBT_KEY = "display";

    public NbtElement toNbt(ItemStack stack, RegistryWrapper.WrapperLookup wrapperLookup, NbtElement nbtElement) {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Cannot encode empty ItemStack");
        }
        #if MC_VER <= MC_1_20_4
        return ItemStack.CODEC
            .encode(stack, NbtOps.INSTANCE, nbtElement)
            .getOrThrow(true, string -> LogUtil.debug("Failed to encode item: {}", string));
        #elif MC_VER > MC_1_20_4
        return ItemStack.CODEC
            .encode(stack, wrapperLookup.getOps(NbtOps.INSTANCE), nbtElement)
            .getOrThrow();
        #endif

    }

    public NbtElement encodeAllowEmpty(ItemStack stack, RegistryWrapper.WrapperLookup wrapperLookup) {
        return stack.isEmpty() ? new NbtCompound() : StackHelper.toNbt(stack, wrapperLookup, new NbtCompound());
    }

    public static Optional<ItemStack> fromNbt(RegistryWrapper.WrapperLookup wrapperLookup, NbtElement nbtElement) {
        return ItemStack.CODEC
            #if MC_VER <= MC_1_20_4
            .parse(NbtOps.INSTANCE, nbtElement)
            #elif MC_VER > MC_1_20_4
            .parse(wrapperLookup.getOps(NbtOps.INSTANCE), nbtElement)
            #endif
            .resultOrPartial(string -> LogUtil.debug("Failed to decode item: '{}'", string));
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
        #if MC_VER <= MC_1_20_4
        return stack
            .getOrCreateSubNbt("display")
            .getList("Lore", NbtElement.STRING_TYPE)
            .stream()
            .map(tag -> TextHelper.fromJson(tag.asString()))
            .collect(Collectors.toList());
        #elif MC_VER > MC_1_20_4
            return stack.get(DataComponentTypes.LORE)
                .comp_2400();
        #endif
    }

    public static void setLore(ItemStack stack, List<Text> texts) {
        #if MC_VER <= MC_1_20_4
        NbtCompound display = stack.getOrCreateSubNbt("display");
        NbtList loreItems = new NbtList();
        for (Text text : texts) {
            loreItems.add(NbtString.of(TextHelper.toJson(text)));
        }
        display.put("Lore", loreItems);
        #elif MC_VER > MC_1_20_4
            LoreComponent loreComponent = new LoreComponent(texts);
            stack.set(DataComponentTypes.LORE, loreComponent);
        #endif
    }

    public static NbtCompound getSkullOwner(ItemStack stack) {
        #if MC_VER <= MC_1_20_4
        return stack.getSubNbt("SkullOwner");
        #elif MC_VER > MC_1_20_4 && MC_VER < MC_1_21_5
        NbtCompound nbt = NbtHelper.getNbt(stack);
        if (nbt == null) return null;
        return nbt.getCompound("SkullOwner");
        #elif MC_VER >= MC_1_21_5
        NbtCompound nbt = NbtHelper.getNbt(stack);
        if (nbt == null) return null;
        return nbt.getCompound("SkullOwner").get();
        #endif
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

    public static boolean filterItemStack(@Nullable ItemStack itemStack, String keyword) {
        /* Nobody wants to get a null item. */
        if (itemStack == null) return false;
        if (itemStack.isEmpty()) return false;

        /* Filter by item name. */
        if (filterItemName(itemStack, keyword)) return true;

        /* Filter by item lore. */
        return filterItemLore(itemStack, keyword);
    }

    @SuppressWarnings("RedundantIfStatement")
    public static boolean filterItemName(ItemStack itemStack, String keyword) {
        String itemName = TextHelper.visitString(itemStack.getName());
        if (itemName
            .toLowerCase()
            .contains(keyword.toLowerCase())) return true;
        return false;
    }

    @SuppressWarnings("RedundantIfStatement")
    public static boolean filterItemLore(ItemStack itemStack, String keyword) {
        boolean matched = getLore(itemStack)
            .stream()
            .anyMatch(text -> TextHelper.visitString(text)
                .toLowerCase()
                .contains(keyword.toLowerCase()));
        if (matched) return true;

        return false;
    }
}
