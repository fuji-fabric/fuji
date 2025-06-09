package io.github.sakurawald.module.initializer.head.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.module.initializer.head.privoder.HeadProvider;
import io.github.sakurawald.module.initializer.head.structure.Category;
import io.github.sakurawald.module.initializer.head.structure.Head;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HeadGui extends SimpleGui {

    protected final ServerPlayerEntity player;

    public HeadGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X2, player, false);
        this.player = player;

        int index = 0;
        for (Category category : Category.values()) {
            setCategoryButton(index, category);
            index++;
        }

        this.setTitle(TextHelper.getTextByKey(player, "head.title"));
        this.setSlot(this.getSize() - 1, new GuiElementBuilder()
            .setItem(Items.COMPASS)
            .setName(TextHelper.getTextByKey(player, "search"))
            .setCallback(() -> new SearchHeadsInputGui(this).open()));
        this.setSlot(this.getSize() - 2, new GuiElementBuilder()
            .setItem(Items.PLAYER_HEAD)
            .setName(TextHelper.getTextByKey(player, "head.category.player"))
            .setCallback(() -> new PlayerHeadGui(this).open()));
    }

    private void setCategoryButton(int slotIndex, @NotNull Category category) {
        this.setSlot(slotIndex, category.toItemStack(player), (a, b, c, d) -> {
            List<Head> entities = new ArrayList<>(HeadProvider.getLoadedHeads().get(category));
            Text title = category.getText(player);
            new CategoryHeadGui(this, player, title, entities, 0).open();
        });
    }

}
