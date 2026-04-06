package mod.fuji.module.initializer.warning.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.ConfirmSignGui;
import mod.fuji.core.gui.component.gui.CrudPagedGui;
import mod.fuji.core.gui.component.gui.EditSignGui;
import mod.fuji.core.gui.component.gui.InputSignGui;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.module.initializer.warning.WarningInitializer;
import mod.fuji.module.initializer.warning.service.WarningService;
import mod.fuji.module.initializer.warning.structure.Warning;
import mod.fuji.module.initializer.warning.structure.PlayerWarnings;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ListPlayerWarningsGui extends CrudPagedGui<Warning> {
    private final String targetPlayerName;

    public ListPlayerWarningsGui(@Nullable SimpleGui parent, @NotNull ServerPlayer player, String targetPlayerName, @NotNull List<Warning> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "warning.list.gui.title", targetPlayerName), entities, pageIndex);
        this.targetPlayerName = targetPlayerName;
    }

    @Override
    protected @NotNull PagedGui<Warning> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayer player, Component title, @NotNull List<Warning> entities, int pageIndex) {
        return new ListPlayerWarningsGui(parent, player, this.targetPlayerName, entities, pageIndex);
    }

    public static ListPlayerWarningsGui make(@Nullable SimpleGui parent, @NotNull ServerPlayer player, @NotNull String targetPlayerName) {
        PlayerWarnings playerWarnings = WarningService.getPlayerWarnings(targetPlayerName);
        return new ListPlayerWarningsGui(parent, player, targetPlayerName, playerWarnings.getWarnings(), 0);
    }

    @Override
    protected GuiElementBuilder toGuiElementBuilder(Warning entity) {
        GuiElementBuilder builder = new GuiElementBuilder();

        builder
            .setItem(entity.asItem())
            .setName(TextHelper.getTextByKey(getPlayer(), "warning.list.gui.name"))
            .setLore(entity.asLore(getPlayer()));

        return builder;
    }

    @Override
    protected @NotNull String getGuiHelpLoreKey() {
        return "warning.list.gui.help.lore";
    }

    @Override
    protected void onCreateEntity() {
        new InputSignGui(getPlayer(), null) {
            @Override
            public void onVirtualGuiClose() {
                String warningDescription = joinStrings();
                if (warningDescription.isBlank()) {
                    ListPlayerWarningsGui.make(getParent(), player, targetPlayerName)
                        .open();
                    return;
                }

                String creatorName = PlayerHelper.getPlayerName(player);
                WarningService.createWarning(creatorName, targetPlayerName, warningDescription, null);

                ListPlayerWarningsGui.make(getParent(), player, targetPlayerName)
                    .open();
            }
        }.open();
    }

    @Override
    protected boolean canCreateEntity() {
        return LuckpermsHelper.hasPermission(getPlayer().getUUID(), WarningInitializer.CREATE_WARNINGS_PERMISSION);
    }

    @Override
    protected boolean canReadEntity(Warning entity) {
        return LuckpermsHelper.hasPermission(getPlayer().getUUID(), WarningInitializer.READ_WARNINGS_PERMISSION);
    }

    @Override
    protected boolean canUpdateEntity(Warning entity) {
        return LuckpermsHelper.hasPermission(getPlayer().getUUID(), WarningInitializer.UPDATE_WARNINGS_PERMISSION);
    }

    @Override
    protected boolean canDeleteEntity(Warning entity) {
        return LuckpermsHelper.hasPermission(getPlayer().getUUID(), WarningInitializer.DELETE_WARNINGS_PERMISSION);
    }

    @Override
    protected void onLeftClickEntity(Warning entity) {
        if (!this.canUpdateEntity(entity)) {
            return;
        }

        String originalDescription = entity.getDescription();
        new EditSignGui(getPlayer(), originalDescription) {
            @Override
            public void onVirtualGuiClose() {
                String newDescription = joinStrings();
                entity.setDescription(newDescription);
                WarningInitializer.data.writeStorage();

                ListPlayerWarningsGui.make(getParent(), player, targetPlayerName)
                    .open();
            }
        }.open();

    }

    @Override
    protected void onRightClickEntity(Warning entity) {
        if (!canDeleteEntity(entity)) {
            return;
        }

        new ConfirmSignGui(getPlayer()) {
            @Override
            public void onConfirm() {
                WarningService.deleteWarning(targetPlayerName, entity);
                ListPlayerWarningsGui.make(getParent(), getPlayer(),targetPlayerName)
                    .open();
            }
        }.open();
    }
}
