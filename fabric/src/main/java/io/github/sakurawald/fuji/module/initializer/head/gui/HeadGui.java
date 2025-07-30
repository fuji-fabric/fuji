package io.github.sakurawald.fuji.module.initializer.head.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.head.privoder.HeadProvider;
import io.github.sakurawald.fuji.module.initializer.head.structure.Category;
import io.github.sakurawald.fuji.module.initializer.head.structure.Head;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HeadGui extends SimpleGui {

    public HeadGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X2, player, false);
        this.setTitle(TextHelper.getTextByKey(player, "head.title"));

        /* Place categories buttons. */
        int index = 0;
        for (Category category : Category.values()) {
            placeCategoryButton(index, category);
            index++;
        }

        /* Place player head button. */
        this.setSlot(this.getSize() - 2, new GuiElementBuilder()
            .setItem(Items.PLAYER_HEAD)
            .setName(TextHelper.getTextByKey(player, "head.category.player"))
            .setCallback(() -> new PlayerHeadGui(this).open()));

        /* Place search button. */
        this.setSlot(this.getSize() - 1, GuiHelper
            .makeSearchButton(player)
            .setCallback(() -> new SearchHeadsInputGui(this).open()));

    }

    private void placeCategoryButton(int slotIndex, @NotNull Category category) {
        this.setSlot(slotIndex, category.toItemStack(player), (a, b, c, d) -> {
            List<Head> entities = new ArrayList<>(HeadProvider.getLoadedHeads().get(category));

            /* Wait for data fetching. */
            if (entities.isEmpty()) {
                TextHelper.sendTextByKey(getPlayer(), "data.fetching");
                close();
                return;
            }

            Text title = category.getText(player);
            new CategoryHeadsGui(this, player, title, entities, 0)
                .open();
        });
    }

}
