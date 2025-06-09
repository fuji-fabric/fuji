package io.github.sakurawald.module.initializer.head.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.gui.PagedGui;
import io.github.sakurawald.module.initializer.head.HeadInitializer;
import io.github.sakurawald.module.initializer.head.structure.EconomyType;
import io.github.sakurawald.module.initializer.head.structure.Head;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class CategoryHeadGui extends PagedGui<Head> {

    public CategoryHeadGui(SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<Head> entities, int pageIndex) {
        super(parent, player, title, entities, pageIndex);
    }

    @Override
    protected PagedGui<Head> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<Head> entities, int pageIndex) {
        return new CategoryHeadGui(parent, player, title, entities, pageIndex);
    }

    @Override
    protected GuiElementInterface toGuiElement(Head entity) {
        var builder = GuiElementBuilder.from(entity.toItemStack());
        if (HeadInitializer.head.model().economy_type != EconomyType.FREE) {
            builder.addLoreLine(Text.empty());
            builder.addLoreLine(TextHelper.getTextByKey(getPlayer(), "head.price").copy().append(EconomyType.getCostText()));
        }

        builder.setCallback((index, type, action) -> handleEntityClick(entity, type));
        return builder.build();
    }

    @Override
    protected List<Head> filter(String keywords) {
        return getEntities().stream()
            .filter(head -> head.name.toLowerCase().contains(keywords.toLowerCase())
                || head.getTagsOrEmpty().toLowerCase().contains(keywords.toLowerCase()))
            .collect(Collectors.toList());
    }

    private void handleEntityClick(@NotNull Head head, @NotNull ClickType type) {
        ServerPlayerEntity player = getPlayer();

        ItemStack cursorStack = player.currentScreenHandler.getCursorStack();
        ItemStack headStack = head.toItemStack();

        /* happy debug */
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            LogUtil.debug("head = {}", headStack.get(DataComponentTypes.PROFILE));
        }

        if (cursorStack.isEmpty()) {
            if (type.shift) {
                EconomyType.tryPurchase(player, 1, () -> player.getInventory().insertStack(headStack));
            } else if (type.isMiddle) {
                EconomyType.tryPurchase(player, headStack.getMaxCount(), () -> {
                    headStack.setCount(headStack.getMaxCount());
                    player.currentScreenHandler.setCursorStack(headStack);
                });
            } else {
                EconomyType.tryPurchase(player, 1, () -> player.currentScreenHandler.setCursorStack(headStack));
            }
        } else if (cursorStack.getMaxCount() <= cursorStack.getCount()) {
            //noinspection UnnecessaryReturnStatement
            return;
        } else if (ItemStack.areItemsAndComponentsEqual(headStack, cursorStack)) {
            /* switch click type */
            if (type.isLeft) {
                EconomyType.tryPurchase(player, 1, () -> cursorStack.increment(1));
            } else if (type.isRight) {
                if (HeadInitializer.head.model().economy_type == EconomyType.FREE)
                    cursorStack.decrement(1);
            } else if (type.isMiddle) {
                var amount = headStack.getMaxCount() - cursorStack.getCount();
                EconomyType.tryPurchase(player, amount, () -> {
                    headStack.setCount(headStack.getMaxCount());
                    player.currentScreenHandler.setCursorStack(headStack);
                });
            }
        } else if (HeadInitializer.head.model().economy_type == EconomyType.FREE) {
            player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
        }
    }
}
