package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.StringUtil;
import java.util.ArrayList;
import java.util.function.Consumer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.nbt.NbtList;

#if MC_VER <= MC_1_20_4
import java.util.stream.Collectors;
import net.minecraft.nbt.NbtString;
#elif MC_VER > MC_1_20_4
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
#endif

public class ItemStackHelper {

    private static final String LORE_NBT_KEY = "Lore";
    private static final String DISPLAY_NBT_KEY = "display";

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
            .getOrCreateSubNbt(DISPLAY_NBT_KEY)
            .getList(LORE_NBT_KEY, NbtElement.STRING_TYPE)
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
        NbtCompound display = stack.getOrCreateSubNbt(DISPLAY_NBT_KEY);
        NbtList loreItems = new NbtList();
        for (Text text : texts) {
            loreItems.add(NbtString.of(TextHelper.toJson(text)));
        }
        display.put(LORE_NBT_KEY, loreItems);
        #elif MC_VER > MC_1_20_4
        LoreComponent loreComponent = new LoreComponent(texts);
        stack.set(DataComponentTypes.LORE, loreComponent);
        #endif
    }

    public static NbtCompound getSkullOwner(ItemStack stack) {
        #if MC_VER <= MC_1_20_4
        return stack.getSubNbt("SkullOwner");
        #elif MC_VER > MC_1_20_4 && MC_VER < MC_1_21_5
        NbtCompound nbt = ItemStackHelper.Nbt.getNbt(stack);
        if (nbt == null) return null;
        return nbt.getCompound("SkullOwner");
        #elif MC_VER >= MC_1_21_5
        NbtCompound nbt = ItemStackHelper.Nbt.getNbt(stack);
        if (nbt == null) return null;
        return nbt.getCompound("SkullOwner").get();
        #endif
    }

    public static boolean canCombine(ItemStack a, ItemStack b) {
        if (!a.isOf(b.getItem())) {
            return false;
        }
        if (a.isEmpty() && b.isEmpty()) {
            return true;
        }
        return Objects.equals(Nbt.getNbt(a), Nbt.getNbt(b));
    }

    public static boolean filterItemStack(@Nullable ItemStack itemStack, String keyword) {
        /* Nobody wants to get a null item. */
        if (itemStack == null) return false;
        if (itemStack.isEmpty()) return false;

        /* Filter by item name. */
        if (filterItemName(itemStack, keyword)) return true;

        /* Filter by item material. */
        if (filterItemMaterial(itemStack, keyword)) return true;

        /* Filter by item lore. */
        return filterItemLore(itemStack, keyword);
    }

    private static boolean filterItemMaterial(@NotNull ItemStack itemStack, String keyword) {
        return StringUtil.toLowerCase(RegistryHelper.getIdAsString(itemStack.getItem()))
            .contains(StringUtil.toLowerCase(keyword));
    }

    @SuppressWarnings("RedundantIfStatement")
    private static boolean filterItemName(ItemStack itemStack, String keyword) {
        String itemName = TextHelper.Operators.visitString(itemStack.getName());
        if (StringUtil.toLowerCase(itemName)
            .contains(StringUtil.toLowerCase(keyword))) return true;
        return false;
    }

    @SuppressWarnings("RedundantIfStatement")
    private static boolean filterItemLore(ItemStack itemStack, String keyword) {
        boolean matched = getLore(itemStack)
            .stream()
            .anyMatch(text -> StringUtil.toLowerCase(TextHelper.Operators.visitString(text))
                .contains(StringUtil.toLowerCase(keyword)));
        if (matched) return true;

        return false;
    }

    public static @NotNull Item getItem(@NotNull String identifier) {
        // NOTE: For un-known identifier, it will always return minecraft:air as the dummy item.
        Item item = Registries.ITEM.get(Identifier.tryParse(identifier));
        if (Items.AIR.equals(item)) {
            LogUtil.warn("Failed to find the item {} in registry, we will return BARRIER instead.", identifier);
            return Items.BARRIER;
        }
        return item;
    }

    public static class Nbt {

        public static NbtElement toNbtAllowEmpty(ItemStack stack, RegistryWrapper.WrapperLookup wrapperLookup) {
            /* Return empty NBT if item stack is empty. */
            if (stack.isEmpty()) {
                return new NbtCompound();
            }

            return Nbt.toNbt(stack, wrapperLookup, new NbtCompound());
        }

        public static ItemStack fromNbtOrEmpty(RegistryWrapper.WrapperLookup wrapperLookup, @Nullable NbtCompound nbtCompound) {
            /* Return empty item stack if NBT is empty. */
            if (nbtCompound == null || nbtCompound.isEmpty()) {
                return ItemStack.EMPTY;
            }

            return fromNbt(wrapperLookup, nbtCompound)
                    .orElse(ItemStack.EMPTY);
        }

        public static NbtList writeSlotsNode(@NotNull NbtList node, @NotNull List<ItemStack> stackList) {
            stackList.forEach(itemStack -> {
                NbtElement nbtAllowEmpty = toNbtAllowEmpty(itemStack, RegistryHelper.getDefaultWrapperLookup());
                node.add(nbtAllowEmpty);
            });
            return node;
        }

        public static @NotNull List<ItemStack> readSlotsNode(@Nullable NbtList node) {
            /* Return empty list. */
            if (node == null) {
                return new ArrayList<>();
            }

            /* Map the NBT to ItemStack. */
            List<ItemStack> ret = new ArrayList<>();
            for (int i = 0; i < node.size(); i++) {
                ret.add(fromNbtOrEmpty(RegistryHelper.getDefaultWrapperLookup(), NbtHelper.Primitives.getCompound(node, i)));
            }
            return ret;
        }

        public static void withNbt(ItemStack stack, Consumer<NbtCompound> nbtConsumer) {
            NbtCompound targetNbt = getNbt(stack);
            if (targetNbt == null) {
                targetNbt = new NbtCompound();
            }

            nbtConsumer.accept(targetNbt);
            setNbt(stack, targetNbt);
        }

        public static @Nullable NbtCompound getNbt(@NotNull ItemStack stack) {
            #if MC_VER <= MC_1_20_4
            return stack.getNbt();
            #elif MC_VER > MC_1_20_4
            NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
            return nbtComponent == null ? null : nbtComponent.copyNbt();
            #endif
        }

        public static void setNbt(@NotNull ItemStack stack, @NotNull NbtCompound newNbt) {
            #if MC_VER <= MC_1_20_4
            stack.setNbt(newNbt);
            #elif MC_VER > MC_1_20_4
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(newNbt));
            #endif
        }

        public static NbtElement toNbt(ItemStack stack, RegistryWrapper.WrapperLookup wrapperLookup, NbtElement nbtElement) {
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

        public static @NotNull ItemStack fromNbtOrBarrier(NbtElement nbtElement) {
            Optional<ItemStack> itemStack = fromNbt(RegistryHelper.getDefaultWrapperLookup(), nbtElement);
            return itemStack.orElseGet(Items.BARRIER::getDefaultStack);
        }

        public static Optional<ItemStack> fromNbt(NbtElement nbtElement) {
            return fromNbt(RegistryHelper.getDefaultWrapperLookup(), nbtElement);
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
    }
}
