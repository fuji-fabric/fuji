package mod.fuji.core.gui.component.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import java.util.List;
import mod.fuji.core.gui.structure.GuiElementIR;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CrudPagedGui<T> extends PagedGui<T> {

    public CrudPagedGui(@Nullable SimpleGui parent, @NotNull ServerPlayer player, @NotNull Component prefixTitle, @NotNull List<T> entities, int pageIndex) {
        super(parent, player, prefixTitle, entities, pageIndex);

        if (this.canCreateEntity()) {
            GuiHelper.Placer.setSlotInLastLine(this, 3, GuiHelper.Button
                .makeAddButton(player)
                .setName(TextHelper.getTextByKey(player, "add"))
                .setCallback(this::doCreateEntity)
            );
        }

        GuiHelper.Placer.setSlotInLastLine(this, 4, GuiHelper.Button
            .makeHelpButton(player)
            .setLore(TextHelper.getTextListByKey(player, getGuiHelpLoreKey())));
    }

    private void doCreateEntity() {
        if (!this.canCreateEntity()) {
            TextHelper.sendTextByKey(getPlayer(), "operation.no_permission");
            return;
        }

        this.onCreateEntity();
    }

    @Override
    protected final @NotNull GuiElementIR toGuiElement(@NotNull T entity) {
        /* Hide the entity if no permission to view. */
        if (!this.canReadEntity(entity)) {
            return GuiElementIR.of(new GuiElementBuilder()
                .setItem(Items.BARRIER)
                .setName(TextHelper.getTextByKey(entity, "no_permission"))
                .build());
        }

        /* Let subclass build the object first. */
        GuiElementBuilder builder = this.toGuiElementBuilder(entity);

        /* Set click callback. */
        builder.setCallback(dispatchClickType(entity));

        return GuiElementIR.of(builder.build());
    }

    protected abstract GuiElementBuilder toGuiElementBuilder(T entity);

    protected abstract @NotNull String getGuiHelpLoreKey();

    protected abstract void onCreateEntity();

    protected abstract boolean canCreateEntity();

    protected abstract boolean canReadEntity(T entity);

    protected abstract boolean canUpdateEntity(T entity);

    protected abstract boolean canDeleteEntity(T entity);

    @SuppressWarnings("UnnecessaryReturnStatement")
    private GuiElementInterface.ItemClickCallback dispatchClickType(T entity) {
        return (index, clickType, actionType) -> {
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
