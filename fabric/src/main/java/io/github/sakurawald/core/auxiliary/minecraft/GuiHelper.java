package io.github.sakurawald.core.auxiliary.minecraft;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import lombok.experimental.UtilityClass;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class GuiHelper {

    public static void fill(@NotNull SimpleGui gui, ItemStack itemStack) {
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setSlot(i, itemStack);
        }
    }

    public boolean isInvalidSlotInsidePlayerInventory(int index) {
        return index == 41 || index == 42 || index == 43 || index == 44;
    }

    public static int getRows(ScreenHandlerType<GenericContainerScreenHandler> screenHandlerType) {
        if (screenHandlerType == ScreenHandlerType.GENERIC_9X1) return 1;
        if (screenHandlerType == ScreenHandlerType.GENERIC_9X2) return 2;
        if (screenHandlerType == ScreenHandlerType.GENERIC_9X3) return 3;
        if (screenHandlerType == ScreenHandlerType.GENERIC_9X4) return 4;
        if (screenHandlerType == ScreenHandlerType.GENERIC_9X5) return 5;
        if (screenHandlerType == ScreenHandlerType.GENERIC_9X6) return 6;

        throw new IllegalArgumentException("Unknown screen handler type: " + screenHandlerType);
    }

    public static GuiElementInterface makeBarrier() {
        return new GuiElementBuilder()
            .setItem(Items.BARRIER)
            .hideTooltip()
            .build();
    }

    public static GuiElementInterface makeSlotPlaceholder() {
        return new GuiElementBuilder()
            .setItem(Items.GRAY_STAINED_GLASS_PANE)
            .hideTooltip()
            .build();
    }

    public static GuiElementBuilder makeSkullButton(String skullOwner) {
        return new GuiElementBuilder()
            .setItem(Items.PLAYER_HEAD)
            .setSkullOwner(skullOwner);
    }

    public static GuiElementBuilder makePreviousPageButton(ServerPlayerEntity player) {
        return makeSkullButton(Icon.PREVIOUS_PAGE_ICON)
            .setName(TextHelper.getTextByKey(player, "previous_page"));
    }

    public static GuiElementBuilder makeNextPageButton(ServerPlayerEntity player) {
        return makeSkullButton(Icon.NEXT_PAGE_ICON)
            .setName(TextHelper.getTextByKey(player, "next_page"));
    }

    public static GuiElementBuilder makeBackButton(ServerPlayerEntity player) {
        return makeSkullButton(Icon.PREVIOUS_PAGE_ICON)
            .setName(TextHelper.getTextByKey(player, "back"));
    }

    public static GuiElementBuilder makeSearchButton(ServerPlayerEntity player) {
        return new GuiElementBuilder()
            .setItem(Items.COMPASS)
            .setName(TextHelper.getTextByKey(player, "search"));
    }

    public static GuiElementBuilder makeAddButton(ServerPlayerEntity player) {
        return makeSkullButton(GuiHelper.Icon.PLUS_ICON)
            .setName(TextHelper.getTextByKey(player, "add"));
    }

    public static GuiElementBuilder makeHelpButton(ServerPlayerEntity player) {
        return makeQuestionMarkButton(player)
            .setName(TextHelper.getTextByKey(player, "help"));
    }

    public static GuiElementBuilder makeUnknownPlayerSkull() {
        return makeSkullButton(Icon.PLAYER_PLACEHOLDER_ICON);
    }

    public static GuiElementBuilder makeQuestionMarkButton(ServerPlayerEntity player) {
        return makeSkullButton(Icon.QUESTION_MARK_ICON);
    }

    public static GuiElementBuilder makeHeartButton(ServerPlayerEntity player) {
        return makeSkullButton(Icon.HEART_ICON);
    }

    public static GuiElementBuilder makeLetterAButton(ServerPlayerEntity player) {
        return makeSkullButton(Icon.A_ICON);
    }

    private static class Icon {
        public static final String PLAYER_PLACEHOLDER_ICON = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQ5Y2M1OGFkMjVhMWFiMTZkMzZiYjVkNmQ0OTNjOGY1ODk4YzJiZjMwMmI2NGUzMjU5MjFjNDFjMzU4NjcifX19";

        public static final String PREVIOUS_PAGE_ICON = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWYxMzNlOTE5MTlkYjBhY2VmZGMyNzJkNjdmZDg3YjRiZTg4ZGM0NGE5NTg5NTg4MjQ0NzRlMjFlMDZkNTNlNiJ9fX0=";
        public static final String NEXT_PAGE_ICON = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTNmYzUyMjY0ZDhhZDllNjU0ZjQxNWJlZjAxYTIzOTQ3ZWRiY2NjY2Y2NDkzNzMyODliZWE0ZDE0OTU0MWY3MCJ9fX0=";

        public static final String PLUS_ICON = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDdhMGZjNmRjZjczOWMxMWZlY2U0M2NkZDE4NGRlYTc5MWNmNzU3YmY3YmQ5MTUzNmZkYmM5NmZhNDdhY2ZiIn19fQ==";
        public static final String HEART_ICON = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMDlhNTJjYjUwOTkyZDgzYzU1OTlmZDZlNDFhNmNlOTljZjdmMWU2MjAzNjExOTYzZGMyYzJmZGEwYjU1NTgzIn19fQ==";
        public static final String A_ICON = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDJjZDVhMWI1Mjg4Y2FhYTIxYTZhY2Q0Yzk4Y2VhZmQ0YzE1ODhjOGIyMDI2Yzg4YjcwZDNjMTU0ZDM5YmFiIn19fQ==";
        public static final String QUESTION_MARK_ICON = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmMyNzEwNTI3MTllZjY0MDc5ZWU4YzE0OTg5NTEyMzhhNzRkYWM0YzI3Yjk1NjQwZGI2ZmJkZGMyZDZiNWI2ZSJ9fX0=";

    }

}
