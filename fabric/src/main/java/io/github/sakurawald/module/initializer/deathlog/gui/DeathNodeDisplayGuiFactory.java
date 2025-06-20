package io.github.sakurawald.module.initializer.deathlog.gui;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.core.auxiliary.minecraft.InventoryHelper;
import io.github.sakurawald.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.service.display.gui.InventoryDisplayGuiFactory;
import io.github.sakurawald.module.initializer.deathlog.structure.DeathNode;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class DeathNodeDisplayGuiFactory extends InventoryDisplayGuiFactory {

    private final DeathNode deathNode;

    public DeathNodeDisplayGuiFactory(@NotNull SimpleGui parent, DeathNode deathNode) {
        super(parent, TextHelper.getTextByKey(parent.getPlayer(), "deathlog.death_node.view.gui.title"), deathNode.main, deathNode.armor, deathNode.offhand);
        this.deathNode = deathNode;
    }

    @Override
    public @NotNull SimpleGui build(ServerPlayerEntity viewerPlayer) {
        SimpleGui displayGui = super.build(viewerPlayer);

        /* Place the back button. */
        GuiElement backButton = GuiHelper.makeBackButton(viewerPlayer)
            .setCallback(() -> {
                displayGui.close();
            }).build();
        displayGui.setSlot(LINE_SIZE, backButton);

        /* Place the restore button. */
        GuiElement restoreButton = new GuiElementBuilder()
            .setItem(Items.SLIME_BALL)
            .setName(TextHelper.getTextByKey(viewerPlayer, "deathlog.restore.item.name"))
            .setCallback(() -> handleRestoreButton(displayGui, viewerPlayer))
            .build();
        displayGui.setSlot(LINE_SIZE + 8, restoreButton);

        /* Check if the NBT format is supported. */
        checkIfCurrentNbtFormatSupported(displayGui);

        return displayGui;
    }

    private void checkIfCurrentNbtFormatSupported(SimpleGui displayGui) {
        boolean notSupported = this.main.stream().allMatch(ItemStack::isEmpty)
            && this.armor.stream().allMatch(ItemStack::isEmpty)
            && this.offhand.stream().allMatch(ItemStack::isEmpty);

        if (notSupported) {
            GuiElement errorNotification = new GuiElementBuilder(Items.BARRIER)
                .setName(TextHelper.getTextByKey(displayGui.getPlayer(), "deathlog.death_node.not_supported_nbt_format"))
                .build();

            for (int i = 0; i < LINE_SIZE * 6; i++) {
                if (displayGui.getSlot(i).getItemStack().isEmpty()) {
                    displayGui.setSlot(i, errorNotification);
                }
            }
        }
    }

    private void handleRestoreButton(SimpleGui displayGui, ServerPlayerEntity viewerPlayer) {
            /* Ensure the restore target player's inventory is empty. */
            if (!viewerPlayer.getInventory().isEmpty()) {
                TextHelper.sendMessageByKey(viewerPlayer, "deathlog.restore.target_player.inventory_not_empty", PlayerHelper.getName(viewerPlayer));
                return;
            }

            /* Restore the inventory. */
            for (int i = 0; i < this.main.size(); i++) {
                InventoryHelper.getMainStacks(viewerPlayer).set(i, this.deathNode.main.get(i));
            }
            InventoryHelper.setArmorStacks(viewerPlayer, this.deathNode.armor);
            InventoryHelper.setOffhandStacks(viewerPlayer, this.deathNode.offhand);
            viewerPlayer.setScore(this.deathNode.score);
            viewerPlayer.experienceLevel = this.deathNode.expLevel;
            viewerPlayer.experienceProgress = this.deathNode.expProgress;

            TextHelper.sendMessageByKey(viewerPlayer, "deathlog.restore.success");
    }
}
