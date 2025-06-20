package io.github.sakurawald.module.initializer.deathlog.gui;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.InventoryHelper;
import io.github.sakurawald.core.service.display.gui.InventoryDisplayGuiFactory;
import io.github.sakurawald.module.initializer.deathlog.structure.DeathNode;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class DeathNodeDisplayGuiFactory extends InventoryDisplayGuiFactory {

    private final DeathNode deathNode;

    public DeathNodeDisplayGuiFactory(@Nullable SimpleGui parent, DeathNode deathNode) {
        super(parent, Text.literal("death node display gui factory"), deathNode.main, deathNode.armor, deathNode.offhand);
        this.deathNode = deathNode;
    }

    @Override
    public @NotNull SimpleGui build(ServerPlayerEntity viewerPlayer) {
        SimpleGui displayGui = super.build(viewerPlayer);

        /* Place the restore button. */
        GuiElement restoreButton = new GuiElementBuilder()
            .setItem(Items.SLIME_BALL)
            .setCallback(() -> handleRestoreButton(viewerPlayer))
            .build();
        displayGui.setSlot(LINE_SIZE + 8, restoreButton);

        return displayGui;
    }

    private void handleRestoreButton(ServerPlayerEntity viewerPlayer) {
            /* Ensure the restore target player's inventory is empty. */
            if (!viewerPlayer.getInventory().isEmpty()) {
                viewerPlayer.sendMessage(Text.literal("Your inventory is not empty."));
//                TextHelper.sendMessageByKey(viewerPlayer, "deathlog.restore.target_player.inventory_not_empty", to.getGameProfile().getName());
//                throw new AbortCommandExecutionException();
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
    }
}
