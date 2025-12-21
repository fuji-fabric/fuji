package mod.fuji.core.auxiliary.minecraft;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import java.util.Map;
import java.util.function.Function;
import mod.fuji.core.auxiliary.AsyncUtil;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.config.mapper.structure.GameProfileIR;
import mod.fuji.core.service.cache.service.GameProfileCacheService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiHelper {

    public static class Handler {

        public static MenuType<ChestMenu> getGenericContainerType(int rows) {
            if (rows == 1) return MenuType.GENERIC_9x1;
            if (rows == 2) return MenuType.GENERIC_9x2;
            if (rows == 3) return MenuType.GENERIC_9x3;
            if (rows == 4) return MenuType.GENERIC_9x4;
            if (rows == 5) return MenuType.GENERIC_9x5;
            if (rows == 6) return MenuType.GENERIC_9x6;

            LogUtil.warn("The rows {} should be in the range [1, 6]. Falling back to GENERIC_9X6.", rows);
            return MenuType.GENERIC_9x6;
        }

        public static int getGenericContainerRows(MenuType<ChestMenu> screenHandlerType) {
            if (screenHandlerType == MenuType.GENERIC_9x1) return 1;
            if (screenHandlerType == MenuType.GENERIC_9x2) return 2;
            if (screenHandlerType == MenuType.GENERIC_9x3) return 3;
            if (screenHandlerType == MenuType.GENERIC_9x4) return 4;
            if (screenHandlerType == MenuType.GENERIC_9x5) return 5;
            if (screenHandlerType == MenuType.GENERIC_9x6) return 6;

            throw new IllegalArgumentException("Unknown screen handler type: " + screenHandlerType);
        }
    }

    public static class PlayerSkull {

        private static @NotNull GuiElementBuilder fromSlot(@NotNull GuiElementInterface slot) {
            GuiElementBuilder builder = new GuiElementBuilder();
            ItemStack itemStack = slot.getItemStack();

            /* Copy data from slot into builder. */
            builder.setItem(itemStack.getItem());
            builder.setName(itemStack.getHoverName());
            List<Component> lore = ItemStackHelper.Lore.getLore(itemStack);
            if (!lore.isEmpty()) {
                builder.setLore(lore);
            }
            builder.setCallback(slot.getGuiCallback());

            return builder;
        }

        public static void fillPlayerHeadTextures(@NotNull SlotGuiInterface gui) {
            fillPlayerHeadTextures(gui, (itemStack) -> itemStack.getHoverName().getString().trim(), () -> {});
        }

        public static void fillPlayerHeadTextures(@NotNull SlotGuiInterface gui, @NotNull Function<ItemStack, String> playerNameMapper, @NotNull Runnable onCompleteCallback) {
            final int logicalSize = gui.getWidth() * (gui.getHeight() - 1);
            for (int i = 0; i < logicalSize; i++) {
                GuiElementInterface targetSlot = gui.getSlot(i);
                if (targetSlot == null) return;

                /* Run async method to fetch game profile. */
                int finalI = i;
                AsyncUtil.runAsyncAndHandleExceptions(() -> {
                    /* Ignore non-player-skull item stack. */
                    ItemStack itemStack = targetSlot.getItemStack();
                    if (!itemStack.getItem().equals(Items.PLAYER_HEAD)) return;

                    /* Get cached game profile. */
                    String playerName = playerNameMapper.apply(itemStack);
                    @NotNull GameProfileIR gameProfileIR = GameProfileCacheService.getCachedGameProfile(playerName);

                    /* Make the gui element builder. */
                    GuiElementBuilder builder = fromSlot(targetSlot);
                    String texturesValue = gameProfileIR.getProperties().toNative().get(AuthlibHelper.TEXTURES_PROPERTY_KEY)
                        .stream()
                        .findFirst()
                        .map(AuthlibHelper::getPropertyValue)
                        .orElse(Texture.LUCKY_BLOCK_TEXTURE);
                    builder.setSkullOwner(texturesValue, null, gameProfileIR.getId());

                    /* Update the target slot. */
                    for (int j = 0; j < logicalSize; j++) {
                        @Nullable GuiElementInterface currentSlot = gui.getSlot(j);
                        if (currentSlot == null) continue;
                        if (currentSlot.getItemStack() == targetSlot.getItemStack()) {
                            gui.setSlot(finalI, builder);
                            break;
                        }
                    }

                    /* Call the post hook. */
                    onCompleteCallback.run();
                });
            }
        }

    }

    public static class Validator {

        private static final Item BANNED_SLOT_PLACEHOLDER_ITEM = Items.BARRIER;

        @SuppressWarnings("RedundantIfStatement")
        public static boolean isBlankSlot(@Nullable GuiElementInterface slot) {
            if (slot == null) return true;
            if (slot.getItemStack() == null) return true;
            if (slot.getItemStack().isEmpty()) return true;

            return false;
        }

        public static boolean isValidSlotIndex(@NotNull SlotGuiInterface gui, int slotIndex) {
            return slotIndex >= 0 && slotIndex < gui.getSize();
        }

        public static boolean isBannedSlotIndex(@NotNull AbstractContainerMenu screenHandler, int index) {
            // NOTE: The index may be -1 or -999 for off-screen action.
            if (index < 0 || index >= screenHandler.slots.size()) return false;

            /* If the index is inside the screen, try to get the slot itemstack. */
            Slot slot = screenHandler.getSlot(index);
            ItemStack stack = slot.getItem();
            return isBannedSlotPlaceholder(stack);
        }

        public static ItemStack makeBannedSlotPlaceholderItemStack() {
            return makeBannedSlotPlaceholder().getItemStack();
        }

        public static boolean isBannedSlotPlaceholder(ItemStack stack) {
            return stack.getItem().equals(BANNED_SLOT_PLACEHOLDER_ITEM);
        }

        public static GuiElementInterface makeBannedSlotPlaceholder() {
            return hideTooltip(new GuiElementBuilder().setItem(BANNED_SLOT_PLACEHOLDER_ITEM))
                .build();
        }
    }

    public static GuiElementBuilder hideTooltip(GuiElementBuilder builder) {
        // NOTE: MC <= 1.20.4, the hideFlags() will not hide the item name.
        // NOTE: In higher MC version, hides the tooltip will also hide the lore.

        #if MC_VER <= MC_1_20_4
            builder.hideFlags();
        #elif MC_VER > MC_1_20_4
            builder.hideTooltip();
        #endif

        return builder;
    }

    public static class Button {

        public static GuiElementInterface makeSlotPlaceholderButton() {
            return hideTooltip(new GuiElementBuilder().setItem(Items.GRAY_STAINED_GLASS_PANE))
                .build();
        }

        public static GuiElementBuilder makePlayerHeadButton(String skullOwner) {
            return new GuiElementBuilder()
                .setItem(Items.PLAYER_HEAD)
                .setSkullOwner(skullOwner);
        }

        public static GuiElementBuilder makePreviousPageButton(ServerPlayer player) {
            return makePlayerHeadButton(Texture.PREVIOUS_PAGE_TEXTURE)
                .setName(TextHelper.getTextByKey(player, "previous_page"));
        }

        public static GuiElementBuilder makeNextPageButton(ServerPlayer player) {
            return makePlayerHeadButton(Texture.NEXT_PAGE_TEXTURE)
                .setName(TextHelper.getTextByKey(player, "next_page"));
        }

        public static GuiElementBuilder makeBackButton(ServerPlayer player) {
            return makePlayerHeadButton(Texture.PREVIOUS_PAGE_TEXTURE)
                .setName(TextHelper.getTextByKey(player, "back"));
        }

        public static GuiElementBuilder makeSearchButton(ServerPlayer player) {
            return new GuiElementBuilder()
                .setItem(Items.COMPASS)
                .setName(TextHelper.getTextByKey(player, "search"))
                .setLore(TextHelper.getTextListByKey(player, "search.lore"))
                .glow();
        }

        public static GuiElementBuilder makeAddButton(ServerPlayer player) {
            return makePlayerHeadButton(Texture.PLUS_TEXTURE)
                .setName(TextHelper.getTextByKey(player, "add"));
        }

        public static GuiElementBuilder makeHelpButton(ServerPlayer player) {
            return makeQuestionMarkButton()
                .setName(TextHelper.getTextByKey(player, "help"));
        }

        public static GuiElementBuilder makeInfoButton(ServerPlayer player) {
            return makeLetterIButton()
                .setName(TextHelper.getTextByKey(player, "info"));
        }

        public static GuiElementBuilder makeLuckyBlockButton() {
            return makePlayerHeadButton(Texture.LUCKY_BLOCK_TEXTURE);
        }

        public static GuiElementBuilder makeQuestionMarkButton() {
            return makePlayerHeadButton(Texture.QUESTION_MARK_TEXTURE);
        }

        public static GuiElementBuilder makeHeartButton() {
            return makePlayerHeadButton(Texture.HEART_TEXTURE);
        }

        public static GuiElementBuilder makeLetterAButton() {
            return makePlayerHeadButton(Texture.LETTER_A_TEXTURE);
        }

        public static GuiElementBuilder makeLetterIButton() {
            return makePlayerHeadButton(Texture.LETTER_I_TEXTURE);
        }

        public static GuiElementBuilder makeModIconButton() {
            return makePlayerHeadButton(Texture.REIMU_HAKUREI_TEXTURE);
        }

    }

    public static class Placer {

        public static void fillEmptySlots(@NotNull SlotGuiInterface gui, @NotNull GuiElementBuilder builder) {
            fillEmptySlots(gui, builder.build());
        }

        public static void fillEmptySlots(@NotNull SlotGuiInterface gui, @NotNull GuiElementInterface guiElementInterface) {
            for (int i = 0; i < gui.getSize(); i++) {
                GuiElementInterface slot = gui.getSlot(i);
                if (Validator.isBlankSlot(slot)) {
                    gui.setSlot(i, guiElementInterface);
                }
            }
        }

        public static void setSlotInLastLine(@NotNull SlotGuiInterface gui, int inlineOffset, @NotNull GuiElementBuilder elementBuilder) {
            setSlotInLastLine(gui, inlineOffset, elementBuilder.build());
        }

        public static void setSlotInLastLine(@NotNull SlotGuiInterface gui, int inlineOffset, @NotNull GuiElementInterface element) {
            final int lastLineIndex = gui.getHeight() - 1;
            setSlotInSpecifiedLine(gui, lastLineIndex, inlineOffset, element);
        }

        public static void setSlotInSpecifiedLine(@NotNull SlotGuiInterface gui, int lineIndex, int inlineOffset, @NotNull GuiElementInterface element) {
            final int baseIndex = lineIndex * gui.getWidth();
            int slotIndex = baseIndex + inlineOffset;
            gui.setSlot(slotIndex, element);
        }

        public static void fillLastLineIfEmpty(@NotNull SlotGuiInterface gui, @NotNull GuiElementInterface element) {
            fillLastLineIf(gui, element, ($gui, slotIndex) -> Optional
                .ofNullable($gui.getSlot(slotIndex))
                .map(GuiElementInterface::getItemStack)
                .map(ItemStack::isEmpty)
                .orElse(true));
        }

        public static void fillLastLineIf(@NotNull SlotGuiInterface gui, @NotNull GuiElementInterface element, @NotNull BiPredicate<SlotGuiInterface, Integer> predicate) {
            final int lastLineIndex = gui.getHeight() - 1;
            fillLineIf(gui, lastLineIndex, element, predicate);
        }

        @SuppressWarnings("unused")
        public static void fillLine(@NotNull SlotGuiInterface gui, int lineIndex, @NotNull GuiElementInterface element) {
            fillLineIf(gui, lineIndex, element, (a, b) -> true);
        }

        public static void fillLineIf(@NotNull SlotGuiInterface gui, int lineIndex, @NotNull GuiElementInterface element, @NotNull BiPredicate<SlotGuiInterface, Integer> predicate) {
            final int lineWidth = gui.getWidth();
            final int baseIndex = lineIndex * lineWidth;

            for (int i = baseIndex; i < baseIndex + lineWidth; i++) {
                if (predicate.test(gui, i)) {
                    gui.setSlot(i, element);
                }
            }
        }

        public static void fillGui(@NotNull SlotGuiInterface gui, @NotNull ItemStack itemStack) {
            for (int i = 0; i < gui.getSize(); i++) {
                gui.setSlot(i, itemStack);
            }
        }

        public static List<GuiElementInterface> makeLinePaddingElements(int filledSlotsSize) {
            final int LINE_SIZE = 9;

            int remainder = filledSlotsSize % LINE_SIZE;
            if (remainder == 0) {
                return List.of();
            }

            List<GuiElementInterface> elements = new ArrayList<>();
            int unfilledSlotsSize = LINE_SIZE - remainder;
            for (int i = 0; i < unfilledSlotsSize; i++) {
                elements.add(Button.makeSlotPlaceholderButton());
            }

            return elements;
        }
    }

    private static class Texture {
        private static final String REIMU_HAKUREI_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTYyMDIyMDc4MTQyNCwKICAicHJvZmlsZUlkIiA6ICJiYjdjY2E3MTA0MzQ0NDEyOGQzMDg5ZTEzYmRmYWI1OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJsYXVyZW5jaW8zMDMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjVmMTcyMGM3ODBhNzk1OGI0MWYxNTNlNTA2OWRiNjg2MWJkMjgxYmU0MzJlN2JjNzk0MTE0YTdmNGVjNTJmZCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9";

        private static final String LUCKY_BLOCK_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQ5Y2M1OGFkMjVhMWFiMTZkMzZiYjVkNmQ0OTNjOGY1ODk4YzJiZjMwMmI2NGUzMjU5MjFjNDFjMzU4NjcifX19";

        private static final String PREVIOUS_PAGE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWYxMzNlOTE5MTlkYjBhY2VmZGMyNzJkNjdmZDg3YjRiZTg4ZGM0NGE5NTg5NTg4MjQ0NzRlMjFlMDZkNTNlNiJ9fX0=";
        private static final String NEXT_PAGE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTNmYzUyMjY0ZDhhZDllNjU0ZjQxNWJlZjAxYTIzOTQ3ZWRiY2NjY2Y2NDkzNzMyODliZWE0ZDE0OTU0MWY3MCJ9fX0=";

        private static final String PLUS_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDdhMGZjNmRjZjczOWMxMWZlY2U0M2NkZDE4NGRlYTc5MWNmNzU3YmY3YmQ5MTUzNmZkYmM5NmZhNDdhY2ZiIn19fQ==";
        private static final String HEART_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMDlhNTJjYjUwOTkyZDgzYzU1OTlmZDZlNDFhNmNlOTljZjdmMWU2MjAzNjExOTYzZGMyYzJmZGEwYjU1NTgzIn19fQ==";
        private static final String LETTER_A_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDJjZDVhMWI1Mjg4Y2FhYTIxYTZhY2Q0Yzk4Y2VhZmQ0YzE1ODhjOGIyMDI2Yzg4YjcwZDNjMTU0ZDM5YmFiIn19fQ==";
        private static final String LETTER_I_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTc2OWUyYzEzNGVlNWZjNmRhZWZlNDEyZTRhZjNkNTdkZjlkYmIzY2FhY2Q4ZTM2ZTU5OTk3OWVjMWFjNCJ9fX0=";
        private static final String QUESTION_MARK_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmMyNzEwNTI3MTllZjY0MDc5ZWU4YzE0OTg5NTEyMzhhNzRkYWM0YzI3Yjk1NjQwZGI2ZmJkZGMyZDZiNWI2ZSJ9fX0=";

    }

    public static class Material {

        public static @NotNull Item fromBooleanValue(boolean value) {
            return value ? Items.GREEN_STAINED_GLASS : Items.RED_STAINED_GLASS;
        }

        public static @NotNull Item fromObjectType(@NotNull Optional<Object> objectValue, @NotNull Class<?> objectType) {
            if (Map.class.isAssignableFrom(objectType)) return Items.MAP;

            if (Iterable.class.isAssignableFrom(objectType)) return getIronChainItem();

            if (Boolean.class.isAssignableFrom(objectType)
                || boolean.class.isAssignableFrom(objectType)) {
                /* If the type of field is boolean, try to get its value. */
                return objectValue
                    .map($objectValue -> (Boolean) $objectValue ? Items.GREEN_BANNER : Items.RED_BANNER)
                    .orElse(Items.STRUCTURE_VOID);
            }

            if (String.class.isAssignableFrom(objectType)
                || Character.class.isAssignableFrom(objectType)
                || char.class.isAssignableFrom(objectType)) {
                return Items.STRING;
            }

            if (Byte.class.isAssignableFrom(objectType)
                || byte.class.isAssignableFrom(objectType)
                || Short.class.isAssignableFrom(objectType)
                || short.class.isAssignableFrom(objectType)
                || Integer.class.isAssignableFrom(objectType)
                || int.class.isAssignableFrom(objectType)
                || Long.class.isAssignableFrom(objectType)
                || long.class.isAssignableFrom(objectType)) {
                return Items.REDSTONE;
            }

            if (Float.class.isAssignableFrom(objectType)
                || float.class.isAssignableFrom(objectType)
                || Double.class.isAssignableFrom(objectType)
                || double.class.isAssignableFrom(objectType)) {
                return Items.GLOWSTONE_DUST;
            }

            if (Enum.class.isAssignableFrom(objectType)) {
                return Items.REPEATER;
            }

            return Items.PINK_SHULKER_BOX;
        }

        private static @NotNull Item getIronChainItem() {
            #if MC_VER < MC_1_21_9
            return Items.CHAIN;
            #elif MC_VER >= MC_1_21_9
            return Items.IRON_CHAIN;
            #endif
        }
    }

}
