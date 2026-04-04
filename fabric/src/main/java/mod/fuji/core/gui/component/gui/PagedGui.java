package mod.fuji.core.gui.component.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.ItemStackHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.core.gui.structure.EntityToElementMapping;
import lombok.Getter;
import mod.fuji.core.gui.structure.GuiElementIR;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class PagedGui<T> extends SimpleGui {

    @Getter
    private final @Nullable SimpleGui parent;
    @Getter
    private final List<T> entities;
    private final int pageIndex;
    private final Component prefixTitle;
    @Getter
    protected boolean streamMessageIntoToast = true;

    private final EntityToElementMapping<T> entityToElementMapping = new EntityToElementMapping<>();

    private boolean openParentGuiWhenClose = true;

    public PagedGui(@Nullable SimpleGui parent, @NotNull ServerPlayer player, @NotNull Component prefixTitle, @NotNull List<T> entities, int pageIndex) {
        super(MenuType.GENERIC_9x6, player, false);
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

    protected abstract @NotNull PagedGui<T> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayer player, Component title, @NotNull List<T> entities, int pageIndex);

    private void drawNavigator(int pageIndex) {
        GuiHelper.Placer.fillLastLineIfEmpty(this, GuiHelper.Button.makeSlotPlaceholderButton());
        GuiHelper.Placer.setSlotInLastLine(this, 0, GuiHelper.Button.makePreviousPageButton(getPlayer()).setCallback(() -> tryChangePage(pageIndex - 1)));
        GuiHelper.Placer.setSlotInLastLine(this, this.getWidth() - 1, GuiHelper.Button.makeNextPageButton(getPlayer()).setCallback(() -> tryChangePage(pageIndex + 1)));
        GuiHelper.Placer.setSlotInLastLine(this, this.getWidth() - 2, GuiHelper.Button.makeSearchButton(getPlayer()).setCallback(this::onSearchButtonClicked));
    }

    private void onSearchButtonClicked() {
        new InputSignGui(getPlayer(), null) {
            @Override
            public void onClose() {
                String keyword = joinStrings();
                linkCurrentGuiAndSearch(keyword).open();
            }
        }.open();
    }

    @TestCase(action = "Issue `/fuji` and click `Next Page` button.", targets = {
        "You should see the reimu there, the footer should not be over-drawn."
    })
    protected void drawPagedGui() {
        // Draw title.
        this.drawTitle();

        // Draw entities.
        this.drawEntitiesOnThisPage(entities);

        // Draw navigator.
        this.drawNavigator(pageIndex);
    }

    private void drawEntitiesOnThisPage(@NotNull List<T> entities) {
        int slotIndex = 0;
        for (int i = getEntityBeginIndex(this.pageIndex); i < getEntityEndIndex(this.pageIndex); i++) {
            T entity = entities.get(i);
            GuiHelper.setSlot(this, slotIndex++, makeGuiElementAndBindIt(entity));
        }
    }

    private void tryChangePage(int newPageIndex) {
        int entityBeginIndex = getEntityBeginIndex(newPageIndex);
        if (entityBeginIndex < 0 || entityBeginIndex >= getEntitySize()) return;

        makePage(this.parent, getPlayer(), this.prefixTitle, this.entities, newPageIndex).open();
    }

    @TestCase(action = "Test the `search` button in paged GUI.", targets = {
        "Issue `/fuji`, and search with keyword `a` twice, then close the GUI. The same GUI should not be linked.",
        "Issue `/fuji`, and search with keyword `afk`, then close the GUI. The different GUI should be linked.",
        "Issue `/fuji`, and search with keyword `world`, then the GUI elements in other pages should be initialized for this search.",
    })
    public @NotNull PagedGui<T> linkCurrentGuiAndSearch(@NotNull String keywords) {
        // NOTE: When search with keywords, we should remember previous GUI.
        Component resultTitle = TextHelper.getTextByKey(getPlayer(), "gui.search.title", keywords);
        List<T> resultEntities = filterEntities(keywords);

        /* Skip the linking, if the none entity is filtered. */
        SimpleGui trueParent = this.getBackendGui();
        if (resultEntities.size() == this.getEntitySize()) {
            trueParent = this.parent;
        }

        return makePage(trueParent, getPlayer(), resultTitle, resultEntities, 0);
    }

    @TestCase(action = "Test the `GUI linking` in paged GUI.", targets = {
        "Issue `/fuji`, and click `core` - `About`, then press `Esc` key to close the GUIs.",
        "Issue `/fuji`, and click the `afk` module, to open the module details GUI, then press `Esc` key to close this GUI.",
        "Issue `/fuji`, click `Next Page` button twice, and click any module here, then press `Esc` key to close this GUI.",
    })
    public @NotNull PagedGui<T> skipCurrentGuiAndSearch(@NotNull Predicate<T> predicate) {
        // NOTE: This method is usually called after inspectAll() method, to only filters the GUI elements, and link this GUI to `parent GUI` (The true GUI). In this use-case, we return an intermediate GUI, someone else wil take bits from it.
        Component resultTitle = TextHelper.getTextByKey(getPlayer(), "gui.search.title", "YOU SHOULD NOT SEE THIS");
        List<T> resultEntities = entities.stream()
            .filter(predicate)
            .toList();
        return makePage(this.parent, getPlayer(), resultTitle, resultEntities, 0);
    }

    protected abstract @NotNull GuiElementIR toGuiElement(@NotNull T entity);

    private @NotNull GuiElementIR makeGuiElementAndBindIt(@NotNull T entity) {
        var element = this.toGuiElement(entity);
        this.entityToElementMapping.setBinding(entity, element);
        return element;
    }

    protected boolean filterEntity(@NotNull T entity, @NotNull String keyword) {
        return false;
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean combinedFilterEntity(@NotNull T entity, @NotNull String keyword) {
        /* Filter using the displaying GUI item stack. (What you see is what you get) */
        // NOTE: We have to make the GUI element for each entity. It's expensive, but saves time.
        GuiElementIR element = entityToElementMapping.getBinding(entity);
        if (element == null) {
            element = makeGuiElementAndBindIt(entity);
        }

        ItemStack itemStack = element.getNativeValue().getItemStack();
        if (ItemStackHelper.Filter.filterItemStack(itemStack, keyword)) {
            return true;
        }

        /* Filter using the user-defined filter. */
        if (filterEntity(entity, keyword)) {
            return true;
        }

        return false;
    }

    private List<T> filterEntities(@NotNull String keyword) {
        return this.entities
            .stream()
            .filter(entity -> combinedFilterEntity(entity, keyword))
            // NOTE: Make a modifiable collection.
            .collect(Collectors.toList());
    }

    public List<GuiElementIR> toGuiElements() {
        return this.entities
            .stream()
            .map(this::makeGuiElementAndBindIt)
            .toList();
    }

    private void drawTitle() {
        MutableComponent formatted = this.prefixTitle.copy().append(TextHelper.getTextByKey(getPlayer(), "gui.page.title", this.getCurrentPageNumber(), this.getMaxPageNumber()));
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

    public @NotNull SimpleGui getBackendGui() {
        return this;
    }

    /**
     * This is an internal GUI, but it's stable since years.
     **/
    @TestCase(action = "Issue `/fuji` command, and press `F` key.", targets = {
        "Check the semantics of `SlotGuiInterface#click`, ensure it didn't changed in new version."
    })
    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean click(int index, ClickType type, net.minecraft.world.inventory.ClickType action) {
        var element = super.getSlot(index);

        /* Prevent the `gui callback` invoke if the `F` key is pressed. */
        if (action.equals(net.minecraft.world.inventory.ClickType.SWAP)) {
            this.onSearchButtonClicked();
            return super.onClick(index, type, action, element);
        }

        if (element != null) {
            element.getGuiCallback().click(index, type, action, this);
        }

        return super.onClick(index, type, action, element);
    }

}
