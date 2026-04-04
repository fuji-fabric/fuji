package mod.fuji.module.initializer.kit.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.LogicHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.InputSignGui;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.core.gui.structure.GuiElementIR;
import mod.fuji.module.initializer.kit.service.KitService;
import mod.fuji.module.initializer.kit.structure.Kit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class KitEditorGui extends PagedGui<Kit> {

    public KitEditorGui(ServerPlayer player, @NotNull List<Kit> entities, int pageIndex) {
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

    public static KitEditorGui make(@NotNull ServerPlayer player) {
        List<Kit> kits = KitService.readKits();
        return new KitEditorGui(player, kits, 0);
    }

    private void openKitEditingGui(@NotNull ServerPlayer player, @NotNull Kit kit) {
        /* Place kit stacks. */
        int rows = 5;
        SimpleContainer simpleInventory = new SimpleContainer(rows * 9);
        for (int i = 0; i < kit.getStackList().size(); i++) {
            simpleInventory.setItem(i, kit.getStackList().get(i));
        }

        /* Set default items if the kit is empty. */
        if (simpleInventory.isEmpty()) {
            placeDefaultKitItems(simpleInventory);
        }

        /* Place the forbidden zone placeholder items. */
        for (int i = 41; i <= 44; i++) {
            simpleInventory.setItem(i, GuiHelper.Validator.makeBannedSlotPlaceholder()
                .getNativeValue()
                .getItemStack());
        }

        /* Make a generic container GUI for kit editing. */
        SimpleMenuProvider simpleNamedScreenHandlerFactory = new SimpleMenuProvider((i, playerInventory, p) ->
            new ChestMenu(MenuType.GENERIC_9x5, i, playerInventory, simpleInventory, rows) {
                @Override
                public void clicked(int i, int j, ClickType slotActionType, Player playerEntity) {
                    // NOTE: skip BARRIER item stack click.
                    if (GuiHelper.Validator.isBannedSlotIndex(this, i)) return;
                    super.clicked(i, j, slotActionType, playerEntity);
                }

                @Override
                public void removed(Player playerEntity) {
                    super.removed(playerEntity);

                    /* Re-create the kit with modified stacks. */
                    List<ItemStack> newStacks = new ArrayList<>();
                    for (int j = 0; j < simpleInventory.getContainerSize(); j++) {
                        newStacks.add(simpleInventory.getItem(j));
                    }
                    KitService.createKit(kit.withStackList(newStacks));
                }

            }, TextHelper.getTextByKey(player, "kit.gui.editor.kit.title", kit.getName()));
        player.openMenu(simpleNamedScreenHandlerFactory);
    }

    private static void placeDefaultKitItems(SimpleContainer simpleInventory) {
        // Mainhand.
        simpleInventory.setItem(0, Items.IRON_SWORD.getDefaultInstance());

        // Food.
        ItemStack food = Items.BREAD.getDefaultInstance();
        food.setCount(16);
        simpleInventory.setItem(1, food);

        // Armors.
        simpleInventory.setItem(36, Items.IRON_BOOTS.getDefaultInstance());
        simpleInventory.setItem(37, Items.IRON_LEGGINGS.getDefaultInstance());
        simpleInventory.setItem(38, Items.IRON_CHESTPLATE.getDefaultInstance());
        simpleInventory.setItem(39, Items.IRON_HELMET.getDefaultInstance());

        // Offhand.
        simpleInventory.setItem(40, Items.SHIELD.getDefaultInstance());
    }

    @Override
    protected @NotNull PagedGui<Kit> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayer player, Component title, @NotNull List<Kit> entities, int pageIndex) {
        return new KitEditorGui(player, entities, pageIndex);
    }

    @Override
    protected @NotNull GuiElementIR toGuiElement(@NotNull Kit entity) {
        return GuiElementIR.of(new GuiElementBuilder()
            .setItem(Items.CHEST)
            .setName(Component.literal(entity.getName()))
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

            }).build());
    }

}
