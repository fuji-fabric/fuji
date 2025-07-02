package io.github.sakurawald.fuji.core.gui.impl.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CrudPagedGui<T> extends PagedGui<T> {

    public CrudPagedGui(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, @NotNull Text prefixTitle, @NotNull List<T> entities, int pageIndex) {
        super(parent, player, prefixTitle, entities, pageIndex);

        getFooter().setSlot(3, GuiHelper
            .makeAddButton(player)
            .setName(TextHelper.getTextByKey(player, "add"))
            .setCallback(() -> onCreateEntity())
        );

        getFooter().setSlot(4, GuiHelper
            .makeHelpButton(player)
            .setLore(TextHelper.getTextListByKey(player, getGuiHelpLoreKey())));
    }

    protected abstract @NotNull String getGuiHelpLoreKey();

    protected abstract void onCreateEntity();

    @SuppressWarnings("UnnecessaryReturnStatement")
    protected GuiElementInterface.@NotNull ItemClickCallback dispatchClickType(@NotNull SimpleGui backendGui, T entity) {
        return (index, clickType, actionType) -> {
            GuiElementInterface element = backendGui.getSlot(index);

            /* Dispatch click type. */
            if (clickType == ClickType.MOUSE_LEFT) {
                onLeftClickEntity(entity);
                return;
            }
            if (clickType == ClickType.MOUSE_RIGHT) {
                onRightClickEntity(entity);
                return;
            }
            if (clickType == ClickType.MOUSE_LEFT_SHIFT) {
                onLeftShiftClickEntity(entity);
                return;
            }
            if (clickType == ClickType.MOUSE_RIGHT_SHIFT) {
                onRightShiftClickEntity(entity);
                return;
            }
            if (clickType == ClickType.MOUSE_MIDDLE) {
                onMiddleClickEntity(entity);
                return;
            }
        };
    }

    protected void onLeftClickEntity(T entity) {}

    protected void onRightClickEntity(T entity) {}

    protected void onLeftShiftClickEntity(T entity) {}

    protected void onRightShiftClickEntity(T entity) {}

    protected void onMiddleClickEntity(T entity) {}

}
