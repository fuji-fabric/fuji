package mod.fuji.module.initializer.kit.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.LogicHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.InputSignGui;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.module.initializer.kit.service.KitService;
import mod.fuji.module.initializer.kit.structure.Kit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class KitEditorGui extends PagedGui<Kit> {

    public KitEditorGui(ServerPlayerEntity player, @NotNull List<Kit> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "kit.gui.editor.title"), entities, pageIndex);

        /* Make footer. */
        GuiHelper.Placer.setSlotInLastLine(this, 1, GuiHelper.Button.makeHelpButton(player)
            .setLore(TextHelper.getTextListByKey(player, "kit.gui.editor.help.lore")));
        GuiHelper.Placer.setSlotInLastLine(this, 4, GuiHelper.Button.makeAddButton(player).setCallback(() -> new InputSignGui(player, TextHelper.getTextByKey(player, "prompt.input.name")) {

            @Override
            public void onClose() {
                /* Get input kit name. */
                String name = getLine(0).getString().trim();

                LogicHelper.withCancelCheck(player, name.isEmpty(), () -> {
                    openKitEditingGui(getPlayer(), KitService.readKit(name));
                });
            }
        }.open()));
    }

    public static KitEditorGui make(@NotNull ServerPlayerEntity player) {
        List<Kit> kits = KitService.readKits();
        return new KitEditorGui(player, kits, 0);
    }

    private void openKitEditingGui(@NotNull ServerPlayerEntity player, @NotNull Kit kit) {
        /* Place kit stacks. */
        int rows = 5;
        SimpleInventory simpleInventory = new SimpleInventory(rows * 9);
        for (int i = 0; i < kit.getStackList().size(); i++) {
            simpleInventory.setStack(i, kit.getStackList().get(i));
        }

        /* Set default items if the kit is empty. */
        if (simpleInventory.isEmpty()) {
            placeDefaultKitItems(simpleInventory);
        }

        /* Place the forbidden zone placeholder items. */
        for (int i = 41; i <= 44; i++) {
            simpleInventory.setStack(i, GuiHelper.Validator.makeBannedSlotPlaceholder().getItemStack());
        }

        /* Make a generic container GUI for kit editing. */
        SimpleNamedScreenHandlerFactory simpleNamedScreenHandlerFactory = new SimpleNamedScreenHandlerFactory((i, playerInventory, p) ->
            new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X5, i, playerInventory, simpleInventory, rows) {
                @Override
                public void onSlotClick(int i, int j, SlotActionType slotActionType, PlayerEntity playerEntity) {
                    // NOTE: skip BARRIER item stack click.
                    if (GuiHelper.Validator.isBannedSlotIndex(this, i)) return;
                    super.onSlotClick(i, j, slotActionType, playerEntity);
                }

                @Override
                public void onClosed(PlayerEntity playerEntity) {
                    super.onClosed(playerEntity);

                    /* Re-create the kit with modified stacks. */
                    List<ItemStack> newStacks = new ArrayList<>();
                    for (int j = 0; j < simpleInventory.size(); j++) {
                        newStacks.add(simpleInventory.getStack(j));
                    }
                    KitService.createKit(kit.withStackList(newStacks));
                }

            }, TextHelper.getTextByKey(player, "kit.gui.editor.kit.title", kit.getName()));
        player.openHandledScreen(simpleNamedScreenHandlerFactory);
    }

    private static void placeDefaultKitItems(SimpleInventory simpleInventory) {
        // Mainhand.
        simpleInventory.setStack(0, Items.IRON_SWORD.getDefaultStack());

        // Food.
        ItemStack food = Items.BREAD.getDefaultStack();
        food.setCount(16);
        simpleInventory.setStack(1, food);

        // Armors.
        simpleInventory.setStack(36, Items.IRON_BOOTS.getDefaultStack());
        simpleInventory.setStack(37, Items.IRON_LEGGINGS.getDefaultStack());
        simpleInventory.setStack(38, Items.IRON_CHESTPLATE.getDefaultStack());
        simpleInventory.setStack(39, Items.IRON_HELMET.getDefaultStack());

        // Offhand.
        simpleInventory.setStack(40, Items.SHIELD.getDefaultStack());
    }

    @Override
    protected PagedGui<Kit> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<Kit> entities, int pageIndex) {
        return new KitEditorGui(player, entities, pageIndex);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull Kit entity) {
        return new GuiElementBuilder()
            .setItem(Items.CHEST)
            .setName(Text.literal(entity.getName()))
            .setCallback((event) -> {
                // Left Click -> Open Editing GUI for the kit.
                if (event.isLeft) {
                    openKitEditingGui(getPlayer(), entity);
                }

                // Shift + Right Click  -> Delete the kit.
                if (event.shift && event.isRight) {
                    KitService.deleteKit(entity.getName());

                    KitEditorGui.make(getPlayer()).open();

                    TextHelper.sendTextByKey(getPlayer(), "deleted");
                }

            }).build();
    }

}
