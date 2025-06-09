package io.github.sakurawald.core.gui;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.layered.LayeredGui;
import io.github.sakurawald.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.gui.layer.SingleLineLayer;
import lombok.Getter;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class PagedGui<T> extends LayeredGui {

    @Getter
    private final @Nullable SimpleGui parent;
    @Getter
    private final List<T> entities;
    private final int pageIndex;
    private final Text prefixTitle;
    @Getter
    private final SingleLineLayer footer = new SingleLineLayer();

    public PagedGui(@Nullable SimpleGui parent, ServerPlayerEntity player, Text prefixTitle, @NotNull List<T> entities, int pageIndex) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        this.parent = parent;
        this.prefixTitle = prefixTitle;
        this.pageIndex = pageIndex;
        this.entities = entities;
    }

    @Override
    public boolean open() {
        /* evaluating the drawPagedGui() until it's ready to open, this makes it possible to let the subclass initialize its slots, and making it possible to insert new entities in search feature */
        this.drawPagedGui();

        return super.open();
    }

    protected abstract PagedGui<T> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<T> entities, int pageIndex);

    private void drawNavigator(int pageIndex) {
        SingleLineLayer pageLayer = new SingleLineLayer(GuiHelper.makeSlotPlaceholder());
        pageLayer.setSlot(0, GuiHelper.makePreviousPageButton(getPlayer()).setCallback(() -> tryChangePage(pageIndex - 1)));
        pageLayer.setSlot(this.getWidth() - 1, GuiHelper.makeNextPageButton(getPlayer()).setCallback(() -> tryChangePage(pageIndex + 1)));
        pageLayer.setSlot(this.getWidth() - 2, GuiHelper.makeSearchButton(getPlayer()).setCallback(() -> new InputSignGui(getPlayer(), null) {
            @Override
            public void onClose() {
                String keyword = reduceInputOrEmpty();
                search(keyword).open();
            }
        }.open()));
        this.addLayer(pageLayer, 0, this.getHeight() - 1);
    }

    protected void drawPagedGui() {
        // draw title
        this.drawTitle();

        // draw entities
        this.drawPagedGui(entities);

        // draw navigator
        this.drawNavigator(pageIndex);

        // draw footer
        this.addLayer(footer, 0, this.getHeight() - 1);
    }

    private void drawPagedGui(@NotNull List<T> entities) {
        int slotIndex = 0;
        for (int i = getEntityBeginIndex(this.pageIndex); i < getEntityEndIndex(this.pageIndex); i++) {
            T entity = entities.get(i);
            this.setSlot(slotIndex++, toGuiElement(entity));
        }
    }

    private void tryChangePage(int newPageIndex) {
        int entityBeginIndex = getEntityBeginIndex(newPageIndex);
        if (entityBeginIndex < 0 || entityBeginIndex >= getEntitySize()) return;

        make(this.parent, getPlayer(), this.prefixTitle, this.entities, newPageIndex).open();
    }

    protected @NotNull PagedGui<T> search(String keywords) {
        return make(this.parent, getPlayer(), TextHelper.getTextByKey(getPlayer(), "gui.search.title", keywords), filter(keywords), 0);
    }

    @SuppressWarnings("unused")
    protected void addEntity(T entity) {
        this.entities.add(entity);
        this.reopen();
    }

    protected void deleteEntity(T entity) {
        this.entities.remove(entity);
        this.reopen();
    }

    protected void reopen() {
        make(this.parent, getPlayer(), this.prefixTitle, this.entities, 0).open();
    }

    protected abstract GuiElementInterface toGuiElement(T entity);

    protected abstract List<T> filter(String keyword);

    private void drawTitle() {
        MutableText formatted = this.prefixTitle.copy().append(TextHelper.getTextByKey(getPlayer(), "gui.page.title", this.getCurrentPageNumber(), this.getMaxPageNumber()));
        this.setTitle(formatted);
    }

    private int getEntitySize() {
        return this.entities.size();
    }

    private int getCurrentPageNumber() {
        return this.pageIndex + 1;
    }

    private int getMaxPageNumber() {
        // edge-case
        if (this.getEntitySize() == 0) return 1;


        int a = this.getEntitySize();
        int b = this.getEntityPageSize();
        int bias = 0;
        if (a % b != 0) bias = 1;
        return a / b + bias;
    }

    private int getEntityPageSize() {
        return (this.getWidth() * this.getHeight()) - 9;
    }

    private int getEntityBeginIndex(int pageIndex) {
        return this.getEntityPageSize() * pageIndex;
    }

    private int getEntityEndIndex(int pageIndex) {
        return Math.min(getEntityBeginIndex(pageIndex + 1), this.getEntitySize());
    }

    @Override
    public void onClose() {
        if (this.parent != null) {
            parent.open();
        }
    }

    protected @Nullable SimpleGui getGui() {
        return this.gui;
    }
}
