package io.github.sakurawald.fuji.core.gui;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.layered.LayeredGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.StackHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.layer.SingleLineLayer;
import io.github.sakurawald.fuji.core.gui.structure.EntityToElementMapper;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class PagedGui<T> extends LayeredGui {

    @Getter
    private final @Nullable SimpleGui parent;
    @Getter
    private final List<T> entities;
    private final int pageIndex;
    private final Text prefixTitle;
    @Getter
    private final SingleLineLayer footer = new SingleLineLayer();

    private final EntityToElementMapper<T> entityToElementMapper = new EntityToElementMapper<>();

    private boolean openParentGuiWhenClose = true;

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
        SingleLineLayer pageLayer = new SingleLineLayer(GuiHelper.makeSlotPlaceholderButton());
        pageLayer.setSlot(0, GuiHelper.makePreviousPageButton(getPlayer()).setCallback(() -> tryChangePage(pageIndex - 1)));
        pageLayer.setSlot(this.getWidth() - 1, GuiHelper.makeNextPageButton(getPlayer()).setCallback(() -> tryChangePage(pageIndex + 1)));
        pageLayer.setSlot(this.getWidth() - 2, GuiHelper.makeSearchButton(getPlayer()).setCallback(() -> new InputSignGui(getPlayer(), null) {
            @Override
            public void onClose() {
                String keyword = reduceInputOrEmpty();
                saveCurrentGuiAndSearch(keyword).open();
            }
        }.open()));
        this.addLayer(pageLayer, 0, this.getHeight() - 1);
    }

    protected void drawPagedGui() {
        // Draw title.
        this.drawTitle();

        // Draw entities.
        this.drawPagedGui(entities);

        // Draw navigator.
        this.drawNavigator(pageIndex);

        // Draw footer.
        this.addLayer(footer, 0, this.getHeight() - 1);
    }

    private void drawPagedGui(@NotNull List<T> entities) {
        int slotIndex = 0;
        for (int i = getEntityBeginIndex(this.pageIndex); i < getEntityEndIndex(this.pageIndex); i++) {
            T entity = entities.get(i);
            this.setSlot(slotIndex++, makeGuiElementAndBindIt(entity));
        }
    }

    private void tryChangePage(int newPageIndex) {
        int entityBeginIndex = getEntityBeginIndex(newPageIndex);
        if (entityBeginIndex < 0 || entityBeginIndex >= getEntitySize()) return;

        make(this.parent, getPlayer(), this.prefixTitle, this.entities, newPageIndex).open();
    }

    public @NotNull PagedGui<T> saveCurrentGuiAndSearch(String keywords) {
        // NOTE: When search with keywords, we should remember previous GUI.
        Text resultTitle = TextHelper.getTextByKey(getPlayer(), "gui.search.title", keywords);
        List<T> resultEntities = filterEntities(keywords);
        return make(this.gui, getPlayer(), resultTitle, resultEntities, 0);
    }

    public @NotNull PagedGui<T> skipCurrentGuiAndSearch(Predicate<T> predicate) {
        // NOTE: This method is usually called after inspectAll() method, to only filters the GUI elements, and link this GUI to `parent GUI` (The true GUI). In this use-case, we return an intermediate GUI, someone else wil take bits from it.
        Text resultTitle = TextHelper.getTextByKey(getPlayer(), "gui.search.title", "YOU SHOULD NOT SEE THIS");
        List<T> resultEntities = entities.stream()
            .filter(predicate)
            .toList();
        return make(this.parent, getPlayer(), resultTitle, resultEntities, 0);
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

    private @NotNull GuiElementInterface makeGuiElementAndBindIt(T entity) {
        GuiElementInterface element = this.toGuiElement(entity);
        this.entityToElementMapper.setBinding(entity, element);
        return element;
    }

    protected abstract boolean filterEntity(T entity, String keyword);

    private boolean combinedFilterEntity(T entity, String keyword) {
        /* Filter using the displaying GUI item stack. (What you see is what you get) */
        // NOTE: We have to make the GUI element for each entity. It's expensive, but saves time.
        GuiElementInterface element = entityToElementMapper.getBinding(entity);
        if (element == null) {
            element = makeGuiElementAndBindIt(entity);
        }

        ItemStack itemStack = element.getItemStack();
        if (StackHelper.filterItemStack(itemStack, keyword)) {
            return true;
        }

        /* Filter using the user-defined filter. */
        if (filterEntity(entity, keyword)) {
            return true;
        }

        return false;
    }

    private List<T> filterEntities(String keyword) {
        return this.entities
            .stream()
            .filter(entity -> combinedFilterEntity(entity, keyword))
            // NOTE: Make a modifiable collection.
            .collect(Collectors.toList());
    }

    public List<GuiElementInterface> toGuiElements() {
        return this.entities
            .stream()
            .map(this::makeGuiElementAndBindIt)
            .toList();
    }

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
        if (this.openParentGuiWhenClose
            && this.parent != null) {
            parent.open();
        }
    }

    public void closeWithoutOpenParentGui() {
        this.openParentGuiWhenClose = false;
        this.close();
    }

    public @Nullable SimpleGui getBackendGui() {
        return this.gui;
    }
}
