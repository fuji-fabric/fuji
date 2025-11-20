package mod.fuji.core.auxiliary.minecraft;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.StringUtil;
import java.util.ArrayList;
import java.util.function.Consumer;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.nbt.ListTag;

#if MC_VER <= MC_1_20_4
#elif MC_VER > MC_1_20_4
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
#endif

public class ItemStackHelper {

    public static class CustomName {

        public static void removeCustomName(@NotNull ItemStack stack) {
            #if MC_VER <= MC_1_20_4
            stack.removeCustomName();
            #elif MC_VER > MC_1_20_4
            stack.remove(DataComponents.CUSTOM_NAME);
            #endif
        }

        public static void setCustomName(@NotNull ItemStack stack, @NotNull Component customName) {
            #if MC_VER <= MC_1_20_4
            stack.setCustomName(customName);
            #elif MC_VER > MC_1_20_4
            stack.set(DataComponents.CUSTOM_NAME, customName);
            #endif
        }

        public static boolean hasCustomName(@NotNull ItemStack stack) {
            #if MC_VER <= MC_1_20_4
            return stack.hasCustomName();
            #elif MC_VER > MC_1_20_4
            return stack.get(DataComponents.CUSTOM_NAME) != null;
            #endif
        }
    }

    public static class Lore {

        @SuppressWarnings("unused")
        private static final String DISPLAY_NBT_KEY = "display";

        @SuppressWarnings("unused")
        private static final String LORE_NBT_KEY = "Lore";

        public static @NotNull List<Component> getLore(@NotNull ItemStack stack) {
            #if MC_VER <= MC_1_20_4
            return stack
                .getOrCreateSubNbt(DISPLAY_NBT_KEY)
                .getList(LORE_NBT_KEY, NbtElement.STRING_TYPE)
                .stream()
                .map(tag -> TextHelper.Codec.fromJson(tag.asString()))
                .collect(java.util.stream.Collectors.toList());
            #elif MC_VER > MC_1_20_4
                var loreComponent = stack.get(DataComponents.LORE);
                if (loreComponent == null) {
                    return List.of();
                }

                return loreComponent.lines();
            #endif
        }

        public static void setLore(@NotNull ItemStack stack, @NotNull List<Component> texts) {
            #if MC_VER <= MC_1_20_4
            NbtCompound displayTag = stack.getOrCreateSubNbt(DISPLAY_NBT_KEY);
            NbtList loreItemsTag = new NbtList();
            for (Text text : texts) {
                loreItemsTag.add(net.minecraft.nbt.NbtString.of(TextHelper.Codec.toJson(text)));
            }
            displayTag.put(LORE_NBT_KEY, loreItemsTag);
            #elif MC_VER > MC_1_20_4
            ItemLore loreComponent = new ItemLore(texts);
            stack.set(DataComponents.LORE, loreComponent);
            #endif
        }
    }

    public static boolean canCombine(@NotNull ItemStack a, @NotNull ItemStack b) {
        if (!a.is(b.getItem())) {
            return false;
        }
        if (a.isEmpty() && b.isEmpty()) {
            return true;
        }
        return Objects.equals(CustomData.getCustomDataNbt(a), CustomData.getCustomDataNbt(b));
    }

    public static class Parser {

        public static @NotNull ItemStack parseItemStack(@NotNull String itemString) {
            StringReader stringReader = new StringReader(itemString);
            try {
                ItemStack stack;
                ItemInput itemStackArgument = ItemArgument.item(CommandHelper.getCommandRegistryAccess()).parse(stringReader);
                stack = createItemStack(itemStackArgument);
                return stack;
            } catch (CommandSyntaxException e) {
                LogUtil.warn("Failed to parse the item string {} into an ItemStack instance, falling back to minecraft:barrier as the result ItemStack instance.", itemString);
                return Items.BARRIER.getDefaultInstance();
            }

        }

        public static @NotNull ItemStack createItemStack(@NotNull ItemInput itemStackArgument) throws CommandSyntaxException {
            return itemStackArgument.createItemStack(1, false);
        }
    }

    public static class Filter {

        public static boolean filterItemStack(@Nullable ItemStack itemStack, @NotNull String keyword) {
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

        private static boolean filterItemMaterial(@NotNull ItemStack itemStack, @NotNull String keyword) {
            String content = RegistryHelper.getIdAsString(itemStack.getItem());
            return StringUtil.containsIgnoreCase(content, keyword);
        }

        @SuppressWarnings("RedundantIfStatement")
        private static boolean filterItemName(@NotNull ItemStack itemStack, @NotNull String keyword) {
            String itemName = TextHelper.Operators.getString(itemStack.getHoverName());
            if (StringUtil.containsIgnoreCase(itemName, keyword)) return true;

            return false;
        }

        @SuppressWarnings("RedundantIfStatement")
        private static boolean filterItemLore(@NotNull ItemStack itemStack, @NotNull String keyword) {
            boolean matched = Lore.getLore(itemStack)
                .stream()
                .anyMatch(text -> {
                    String content = TextHelper.Operators.getString(text);
                    return StringUtil.containsIgnoreCase(content, keyword);
                });
            if (matched) return true;

            return false;
        }
    }

    public static class CustomData {

        @SuppressWarnings("unused")
        public static void withCustomDataNbt(@NotNull ItemStack stack, @NotNull Consumer<CompoundTag> nbtConsumer) {
            CompoundTag customDataNbt = getCustomDataNbt(stack);
            if (customDataNbt == null) {
                customDataNbt = new CompoundTag();
            }

            nbtConsumer.accept(customDataNbt);
            setCustomDataNbt(stack, customDataNbt);
        }

        /**
 *             Before MC 1.20.5, the user-defined NBT is saved in path <code>tag</code> tree.
            After that, it is saved in <code>components.minecraft:custom_data</code> tree.
            For a NbtCompound, the data schema migration will be done automatically.

 **/
        public static @Nullable CompoundTag getCustomDataNbt(@NotNull ItemStack stack) {
            #if MC_VER <= MC_1_20_4
            return stack.getNbt();
            #elif MC_VER > MC_1_20_4
            net.minecraft.world.item.component.CustomData nbtComponent = stack.get(DataComponents.CUSTOM_DATA);
            return nbtComponent == null ? null : nbtComponent.copyTag();
            #endif
        }

        public static void setCustomDataNbt(@NotNull ItemStack stack, @NotNull CompoundTag newNbt) {
            #if MC_VER <= MC_1_20_4
            stack.setNbt(newNbt);
            #elif MC_VER > MC_1_20_4
            stack.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(newNbt));
            #endif
        }
    }

    public static class Codec {

        private static @NotNull Tag toNbtAllowEmpty(@NotNull ItemStack stack) {
            /* Return empty NBT if item stack is empty. */
            if (stack.isEmpty()) {
                return new CompoundTag();
            }

            return Codec.toNbt(stack, new CompoundTag());
        }

        private static @NotNull ItemStack fromNbtOrEmpty(@Nullable CompoundTag nbtCompound) {
            /* Return empty item stack if NBT is empty. */
            if (nbtCompound == null || nbtCompound.isEmpty()) {
                return ItemStack.EMPTY;
            }

            return fromNbt(nbtCompound)
                .orElse(ItemStack.EMPTY);
        }

        public static ListTag writeSlotsNode(@NotNull ListTag node, @NotNull List<ItemStack> stackList) {
            stackList.forEach(itemStack -> {
                Tag nbtAllowEmpty = toNbtAllowEmpty(itemStack);
                node.add(nbtAllowEmpty);
            });
            return node;
        }

        public static @NotNull List<ItemStack> readSlotsNode(@Nullable ListTag node) {
            /* Return empty list. */
            if (node == null) {
                return new ArrayList<>();
            }

            /* Map the NBT to ItemStack. */
            List<ItemStack> ret = new ArrayList<>();
            for (int i = 0; i < node.size(); i++) {
                CompoundTag nbtCompound = NbtHelper.Primitives
                    .getCompound(node, i)
                    .orElseGet(() -> {
                        LogUtil.warn("Failed to read an item stack from slots node: nbtList = {}", node);
                        return new CompoundTag();
                    });
                ret.add(fromNbtOrEmpty(nbtCompound));
            }
            return ret;
        }

        public static Tag toNbt(@NotNull ItemStack stack, @NotNull Tag nbtElement) {
            if (stack.isEmpty()) {
                throw new IllegalStateException("Cannot encode empty ItemStack");
            }

            #if MC_VER <= MC_1_20_4
            return ItemStack.CODEC
                .encode(stack, NbtOps.INSTANCE, nbtElement)
                .getOrThrow(true, string -> LogUtil.debug("Failed to encode item: {}", string));
            #elif MC_VER > MC_1_20_4
            return ItemStack.CODEC
                .encode(stack, RegistryHelper.getDefaultWrapperLookup().createSerializationContext(NbtOps.INSTANCE), nbtElement)
                .getOrThrow();
            #endif

        }

        public static Optional<ItemStack> fromNbt(@NotNull Tag nbtElement) {
            #if MC_VER <= MC_1_20_4
            return ItemStack.CODEC
                .decode(NbtOps.INSTANCE, nbtElement)
                .map(Pair::getFirst)
                .resultOrPartial(string -> LogUtil.debug("Failed to decode item: '{}'", string));
            #elif MC_VER > MC_1_20_4
            return ItemStack.CODEC
                .decode(RegistryHelper.getDefaultWrapperLookup().createSerializationContext(NbtOps.INSTANCE), nbtElement)
                .map(Pair::getFirst)
                .resultOrPartial(string -> LogUtil.debug("Failed to decode item: '{}'", string));
            #endif
        }
    }
}
