package mod.fuji.module.initializer.deathlog.gui;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.InventoryHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.structure.SimpleGuiDuck;
import mod.fuji.core.gui.structure.SlotGuiInterfaceDuck;
import mod.fuji.core.service.display.gui.InventoryDisplayGuiFactory;
import mod.fuji.module.initializer.deathlog.structure.DeathNode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class DeathNodeDisplayGuiFactory extends InventoryDisplayGuiFactory {

    private final DeathNode deathNode;

    public DeathNodeDisplayGuiFactory(@NotNull SimpleGuiDuck parent, DeathNode deathNode) {
        super(parent, TextHelper.getTextByKey(parent.getPlayer(), "deathlog.death_node.view.gui.title"), deathNode.main, deathNode.armor, deathNode.offhand);
        this.deathNode = deathNode;
    }

    @Override
    public @NotNull SlotGuiInterfaceDuck build(@NotNull ServerPlayer viewingPlayer) {
        SlotGuiInterfaceDuck displayGui = super.build(viewingPlayer);

        /* Place the back button. */
        GuiElement backButton = GuiHelper.Button.makeBackButton(viewingPlayer)
            .setCallback(() -> {
                displayGui.close();
            }).build();
        displayGui.setSlot(GuiHelper.GENERIC_CONTAINER_LINE_SIZE, backButton);

        /* Place the restore button. */
        GuiElement restoreButton = new GuiElementBuilder()
            .setItem(Items.SLIME_BALL)
            .setName(TextHelper.getTextByKey(viewingPlayer, "deathlog.restore.item.name"))
            .setLore(deathNode.getLore(viewingPlayer))
            .setCallback(() -> handleRestoreButton(viewingPlayer))
            .build();
        displayGui.setSlot(GuiHelper.GENERIC_CONTAINER_LINE_SIZE + 8, restoreButton);

        /* Check if the NBT format is supported. */
        checkIfCurrentNbtFormatSupported(displayGui);

        return displayGui;
    }

    private void checkIfCurrentNbtFormatSupported(SlotGuiInterfaceDuck displayGui) {
        boolean notSupported = this.main.stream().allMatch(ItemStack::isEmpty)
            && this.armor.stream().allMatch(ItemStack::isEmpty)
            && this.offhand.stream().allMatch(ItemStack::isEmpty);

        if (notSupported) {
            GuiElement errorNotification = new GuiElementBuilder(Items.BARRIER)
                .setName(TextHelper.getTextByKey(displayGui.getPlayer(), "deathlog.death_node.not_supported_nbt_format"))
                .build();

            for (int i = 0; i < GuiHelper.GENERIC_CONTAINER_LINE_SIZE * 6; i++) {
                var slot = GuiHelper.getSlot(displayGui, i);
                if (slot.getNativeValue() == null) continue;
                if (slot.getNativeValue().getItemStack().isEmpty()) {
                    displayGui.setSlot(i, errorNotification);
                }
            }
        }
    }

    private void handleRestoreButton(ServerPlayer viewingPlayer) {
            /* Ensure the restore target player's inventory is empty. */
            if (!viewingPlayer.getInventory().isEmpty()) {
                TextHelper.sendTextByKey(viewingPlayer, "deathlog.restore.target_player.inventory_not_empty", PlayerHelper.getPlayerName(viewingPlayer));
                return;
            }

            /* Restore the inventory. */
            LogUtil.debug("Restore the death node {} for target player {}", deathNode, PlayerHelper.getPlayerName(viewingPlayer));
            for (int i = 0; i < this.main.size(); i++) {
                InventoryHelper.getMainStacks(viewingPlayer).set(i, this.deathNode.main.get(i));
            }
            InventoryHelper.setArmorStacks(viewingPlayer, this.deathNode.armor);
            InventoryHelper.setOffhandStacks(viewingPlayer, this.deathNode.offhand);
            // NOTE: Yeah, the score and xp should give to the dead player.
            viewingPlayer.setScore(this.deathNode.score);
            viewingPlayer.experienceLevel = this.deathNode.expLevel;
            viewingPlayer.experienceProgress = this.deathNode.expProgress;

            TextHelper.sendTextByKey(viewingPlayer, "deathlog.restore.success");
    }
}
