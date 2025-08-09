package io.github.sakurawald.fuji.module.initializer.head.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.StringUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ItemStackHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.impl.gui.PagedGui;
import io.github.sakurawald.fuji.module.initializer.head.HeadInitializer;
import io.github.sakurawald.fuji.module.initializer.head.structure.EconomyType;
import io.github.sakurawald.fuji.module.initializer.head.structure.Head;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CategoryHeadsGui extends PagedGui<Head> {

    public CategoryHeadsGui(SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<Head> entities, int pageIndex) {
        super(parent, player, title, entities, pageIndex);
    }

    @Override
    protected PagedGui<Head> make(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<Head> entities, int pageIndex) {
        return new CategoryHeadsGui(parent, player, title, entities, pageIndex);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull Head entity) {
        /* Add the price text to the head stack. */
        var builder = GuiElementBuilder.from(entity.toItemStack());
        if (HeadInitializer.config.model().economy_type != EconomyType.FREE) {
            builder.addLoreLine(Text.empty());
            builder.addLoreLine(TextHelper.getTextByKey(getPlayer(), "head.price").copy().append(EconomyType.getCostText()));
        }

        /* Set click callback. */
        builder.setCallback((index, type, action) -> handleEntityClick(entity, type));
        return builder.build();
    }

    @Override
    protected boolean filterEntity(@NotNull Head entity, @NotNull String keywords) {
        return StringUtil.containsIgnoreCase(entity.name, keywords)
            || StringUtil.containsIgnoreCase(entity.getTagsOrEmpty(), keywords);
    }

    private void handleEntityClick(@NotNull Head head, @NotNull ClickType type) {
        ServerPlayerEntity player = getPlayer();
        ItemStack cursorStack = player.currentScreenHandler.getCursorStack();
        ItemStack headStack = head.toItemStack();

        /* If cursor stack is empty, then we are safe to set the cursor stack. */
        if (cursorStack.isEmpty()) {
            if (type.shift) { // Shift click -> buy into inventory.
                EconomyType.tryPurchaseHeads(player, 1, () -> player.getInventory().insertStack(headStack));
            } else if (type.isMiddle) { // Double click -> buy to max count.
                EconomyType.tryPurchaseHeads(player, headStack.getMaxCount(), () -> {
                    headStack.setCount(headStack.getMaxCount());
                    player.currentScreenHandler.setCursorStack(headStack);
                });
            } else { // Single click -> buy one.
                EconomyType.tryPurchaseHeads(player, 1, () -> player.currentScreenHandler.setCursorStack(headStack));
            }
        } else if (ItemStackHelper.canCombine(headStack, cursorStack)) {
            if (type.isLeft) { // Single click -> buy one.
                EconomyType.tryPurchaseHeads(player, 1, () -> cursorStack.increment(1));
            } else if (type.isRight) { // Right click -> only allow to return of goods when it's free.
                if (HeadInitializer.config.model().economy_type == EconomyType.FREE) {
                    cursorStack.decrement(1);
                }
            } else if (type.isMiddle) { // Double click -> buy to max count.
                var amount = headStack.getMaxCount() - cursorStack.getCount();
                EconomyType.tryPurchaseHeads(player, amount, () -> {
                    headStack.setCount(headStack.getMaxCount());
                    player.currentScreenHandler.setCursorStack(headStack);
                });
            }
        }
    }
}
