package mod.fuji.module.initializer.head.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.StringUtil;
import mod.fuji.core.auxiliary.minecraft.ItemStackHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.core.gui.structure.GuiElementIR;
import mod.fuji.module.initializer.head.HeadInitializer;
import mod.fuji.module.initializer.head.structure.EconomyType;
import mod.fuji.module.initializer.head.structure.Head;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CategoryHeadsGui extends PagedGui<Head> {

    public CategoryHeadsGui(SimpleGui parent, ServerPlayer player, Component title, @NotNull List<Head> entities, int pageIndex) {
        super(parent, player, title, entities, pageIndex);
    }

    @Override
    protected @NotNull PagedGui<Head> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayer player, Component title, @NotNull List<Head> entities, int pageIndex) {
        return new CategoryHeadsGui(parent, player, title, entities, pageIndex);
    }

    @Override
    protected @NotNull GuiElementIR toGuiElement(@NotNull Head entity) {
        /* Add the price text to the head stack. */
        var builder = GuiElementBuilder.from(entity.toItemStack());
        if (HeadInitializer.config.model().economy_type != EconomyType.FREE) {
            builder.addLoreLine(Component.empty());
            builder.addLoreLine(TextHelper.getTextByKey(getPlayer(), "head.price").copy().append(EconomyType.getCostText()));
        }

        /* Set click callback. */
        builder.setCallback((index, type, action) -> handleEntityClick(entity, type));
        return GuiElementIR.of(builder.build());
    }

    @Override
    protected boolean filterEntity(@NotNull Head entity, @NotNull String keywords) {
        return StringUtil.containsIgnoreCase(entity.name, keywords)
            || StringUtil.containsIgnoreCase(entity.getTagsOrEmpty(), keywords);
    }

    private void handleEntityClick(@NotNull Head head, @NotNull ClickType type) {
        ServerPlayer player = getPlayer();
        ItemStack cursorStack = player.containerMenu.getCarried();
        ItemStack headStack = head.toItemStack();

        /* If cursor stack is empty, then we are safe to set the cursor stack. */
        if (cursorStack.isEmpty()) {
            if (type.shift) { // Shift click -> buy into inventory.
                EconomyType.tryPurchaseHeads(player, 1, () -> player.getInventory().add(headStack));
            } else if (type.isMiddle) { // Double click -> buy to max count.
                EconomyType.tryPurchaseHeads(player, headStack.getMaxStackSize(), () -> {
                    headStack.setCount(headStack.getMaxStackSize());
                    player.containerMenu.setCarried(headStack);
                });
            } else { // Single click -> buy one.
                EconomyType.tryPurchaseHeads(player, 1, () -> player.containerMenu.setCarried(headStack));
            }
        } else if (ItemStackHelper.canCombine(headStack, cursorStack)) {
            if (type.isLeft) { // Single click -> buy one.
                EconomyType.tryPurchaseHeads(player, 1, () -> cursorStack.grow(1));
            } else if (type.isRight) { // Right click -> only allow to return of goods when it's free.
                if (HeadInitializer.config.model().economy_type == EconomyType.FREE) {
                    cursorStack.shrink(1);
                }
            } else if (type.isMiddle) { // Double click -> buy to max count.
                var amount = headStack.getMaxStackSize() - cursorStack.getCount();
                EconomyType.tryPurchaseHeads(player, amount, () -> {
                    headStack.setCount(headStack.getMaxStackSize());
                    player.containerMenu.setCarried(headStack);
                });
            }
        }
    }
}
