package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import com.mojang.authlib.GameProfile;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import eu.pb4.sgui.api.gui.layered.LayeredGui;
import io.github.sakurawald.fuji.core.auxiliary.AsyncUtil;
import io.github.sakurawald.fuji.core.service.gameprofile_fetcher.MojangProfileFetcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class GuiHelper {

    private static final Item INVALID_SLOT_PLACEHOLDER_ITEM = Items.BARRIER;

    public static void fill(@NotNull SimpleGui gui, ItemStack itemStack) {
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setSlot(i, itemStack);
        }
    }

    public static List<GuiElementInterface> fillUntilEndOfLine(int filledSlotsSize) {
        final int LINE_SIZE = 9;

        int remainder = filledSlotsSize % LINE_SIZE;
        if (remainder == 0) {
            return List.of();
        }

        List<GuiElementInterface> elements = new ArrayList<>();
        int unfilledSlotsSize = LINE_SIZE - remainder;
        for (int i = 0; i < unfilledSlotsSize; i++) {
            elements.add(makeSlotPlaceholderButton());
        }

        return elements;
    }

    public static boolean isInvalidSlotInsidePlayerInventory(int index) {
        return index == 41 || index == 42 || index == 43 || index == 44;
    }

    public static boolean isInvalidSlotPlaceholder(ItemStack stack) {
        return stack.getItem().equals(INVALID_SLOT_PLACEHOLDER_ITEM);
    }

    @SuppressWarnings("IfStatementWithIdenticalBranches")
    public static ScreenHandlerType<GenericContainerScreenHandler> getGenericContainerType(int rows) {
        if (rows == 1) return ScreenHandlerType.GENERIC_9X1;
        if (rows == 2) return ScreenHandlerType.GENERIC_9X2;
        if (rows == 3) return ScreenHandlerType.GENERIC_9X3;
        if (rows == 4) return ScreenHandlerType.GENERIC_9X4;
        if (rows == 5) return ScreenHandlerType.GENERIC_9X5;
        if (rows == 6) return ScreenHandlerType.GENERIC_9X6;

        throw new IllegalArgumentException("The rows should be ranged [1, 6].");
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

    public static GuiElementBuilder fromSlot(GuiElementInterface slot) {
        GuiElementBuilder builder = new GuiElementBuilder();
        ItemStack itemStack = slot.getItemStack();

        /* Copy data from slot into builder. */
        builder.setItem(itemStack.getItem());
        builder.setName(itemStack.getName());
        List<Text> lore = ItemStackHelper.getLore(itemStack);
        if (!lore.isEmpty()) {
            builder.setLore(lore);
        }
        builder.setCallback(slot.getGuiCallback());

        return builder;
    }

    public static boolean isValidSlotIndex(SlotGuiInterface gui, int slotIndex) {
        return slotIndex >= 0 && slotIndex < gui.getSize();
    }

    public static GuiElementBuilder hideTooltip(GuiElementBuilder builder) {
        // NOTE: In higher MC version, hides the tooltip will also hide the lore.

        #if MC_VER <= MC_1_20_4
            builder.hideFlags();
        #elif MC_VER > MC_1_20_4
            builder.hideTooltip();
        #endif

        return builder;
    }

    public static GuiElementBuilder setPlayerHeadTexture(GuiElementBuilder builder, String playerName) {
        builder.setItem(Items.PLAYER_HEAD);

        Optional<GameProfile> gameProfileByName = PlayerHelper.getGameProfileByName(playerName);
        MinecraftServer server = ServerHelper.getServer();

        gameProfileByName.ifPresent(gameProfile -> builder.setSkullOwner(gameProfile, server));
        return builder;
    }

    public static GuiElementInterface makeBarrierButton() {
        return hideTooltip(new GuiElementBuilder().setItem(Items.BARRIER))
            .build();
    }

    public static GuiElementInterface makeSlotPlaceholderButton() {
        return hideTooltip(new GuiElementBuilder().setItem(Items.GRAY_STAINED_GLASS_PANE))
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
            .setName(TextHelper.getTextByKey(player, "search"))
            .glow();
    }

    public static GuiElementBuilder makeAddButton(ServerPlayerEntity player) {
        return makeSkullButton(GuiHelper.Icon.PLUS_ICON)
            .setName(TextHelper.getTextByKey(player, "add"));
    }

    public static GuiElementBuilder makeHelpButton(ServerPlayerEntity player) {
        return makeQuestionMarkButton(player)
            .setName(TextHelper.getTextByKey(player, "help"));
    }

    public static GuiElementBuilder makeInfoButton(ServerPlayerEntity player) {
        return makeLetterIButton(player)
            .setName(TextHelper.getTextByKey(player, "info"));
    }

    public static GuiElementBuilder makeLuckyBlockSkull() {
        return makeSkullButton(Icon.LUCKY_BLOCK_ICON);
    }

    public static GuiElementBuilder makeQuestionMarkButton(ServerPlayerEntity player) {
        return makeSkullButton(Icon.QUESTION_MARK_ICON);
    }

    public static GuiElementBuilder makeHeartButton(ServerPlayerEntity player) {
        return makeSkullButton(Icon.HEART_ICON);
    }

    public static GuiElementBuilder makeLetterAButton(ServerPlayerEntity player) {
        return makeSkullButton(Icon.LETTER_A_ICON);
    }

    public static GuiElementBuilder makeLetterIButton(ServerPlayerEntity player) {
        return makeSkullButton(Icon.LETTER_I_ICON);
    }

    public static void fetchHeads(LayeredGui gui, Runnable drawCallback) {
        final int guiSize = gui.getSize();
        for (int i = 0; i < guiSize; i++) {
            GuiElementInterface previousSlot = gui.getSlot(i);
            if (previousSlot == null) return;

            /* Run async method to fetch game profile. */
            int finalI = i;
            AsyncUtil.runAsyncAndSwallowExceptions(() -> {
                ItemStack itemStack = previousSlot.getItemStack();

                // Fetch the game profile from mojang server.
                String onlinePlayerName = itemStack.getName().getString().trim();
                GameProfile gameProfile = MojangProfileFetcher.makeOnlineGameProfile(onlinePlayerName);

                // Apply the game profile.
                GuiElementBuilder builder = fromSlot(previousSlot);
                builder.setSkullOwner(gameProfile, ServerHelper.getServer());
                for (int j = 0; j < guiSize; j++) {
                    GuiElementInterface currentSlot = gui.getSlot(j);
                    if (currentSlot == null) continue;
                    if (currentSlot.getItemStack() == previousSlot.getItemStack()) {
                        gui.setSlot(finalI, builder);
                        break;
                    }
                }

                // Call draw to re-draw it.
                drawCallback.run();
            });
        }
    }

    public static void fillEmptySlots(SlotGuiInterface gui, GuiElementBuilder builder) {
        fillEmptySlots(gui, builder.build());
    }

    public static void fillEmptySlots(SlotGuiInterface gui, GuiElementInterface guiElementInterface) {
        for (int i = 0; i < gui.getSize(); i++) {
            GuiElementInterface slot = gui.getSlot(i);
            if (slot == null
                || slot.getItemStack() == null
                || slot.getItemStack().isEmpty()) {
                gui.setSlot(i, guiElementInterface);
            }
        }
    }

    private static class Icon {
        private static final String LUCKY_BLOCK_ICON = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQ5Y2M1OGFkMjVhMWFiMTZkMzZiYjVkNmQ0OTNjOGY1ODk4YzJiZjMwMmI2NGUzMjU5MjFjNDFjMzU4NjcifX19";

        private static final String PREVIOUS_PAGE_ICON = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWYxMzNlOTE5MTlkYjBhY2VmZGMyNzJkNjdmZDg3YjRiZTg4ZGM0NGE5NTg5NTg4MjQ0NzRlMjFlMDZkNTNlNiJ9fX0=";
        private static final String NEXT_PAGE_ICON = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTNmYzUyMjY0ZDhhZDllNjU0ZjQxNWJlZjAxYTIzOTQ3ZWRiY2NjY2Y2NDkzNzMyODliZWE0ZDE0OTU0MWY3MCJ9fX0=";

        private static final String PLUS_ICON = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDdhMGZjNmRjZjczOWMxMWZlY2U0M2NkZDE4NGRlYTc5MWNmNzU3YmY3YmQ5MTUzNmZkYmM5NmZhNDdhY2ZiIn19fQ==";
        private static final String HEART_ICON = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMDlhNTJjYjUwOTkyZDgzYzU1OTlmZDZlNDFhNmNlOTljZjdmMWU2MjAzNjExOTYzZGMyYzJmZGEwYjU1NTgzIn19fQ==";
        private static final String LETTER_A_ICON = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDJjZDVhMWI1Mjg4Y2FhYTIxYTZhY2Q0Yzk4Y2VhZmQ0YzE1ODhjOGIyMDI2Yzg4YjcwZDNjMTU0ZDM5YmFiIn19fQ==";
        private static final String LETTER_I_ICON = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTc2OWUyYzEzNGVlNWZjNmRhZWZlNDEyZTRhZjNkNTdkZjlkYmIzY2FhY2Q4ZTM2ZTU5OTk3OWVjMWFjNCJ9fX0=";
        private static final String QUESTION_MARK_ICON = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmMyNzEwNTI3MTllZjY0MDc5ZWU4YzE0OTg5NTEyMzhhNzRkYWM0YzI3Yjk1NjQwZGI2ZmJkZGMyZDZiNWI2ZSJ9fX0=";

    }

    public static class Material {
        public static Item fromBooleanValue(boolean value) {
            return value ? Items.GREEN_STAINED_GLASS : Items.RED_STAINED_GLASS;
        }
    }

}
