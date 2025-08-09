package io.github.sakurawald.fuji.module.initializer.warning.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.impl.gui.ConfirmSignGui;
import io.github.sakurawald.fuji.core.gui.impl.gui.CrudPagedGui;
import io.github.sakurawald.fuji.core.gui.impl.gui.EditSignGui;
import io.github.sakurawald.fuji.core.gui.impl.gui.InputSignGui;
import io.github.sakurawald.fuji.core.gui.impl.gui.PagedGui;
import io.github.sakurawald.fuji.module.initializer.warning.WarningInitializer;
import io.github.sakurawald.fuji.module.initializer.warning.service.WarningService;
import io.github.sakurawald.fuji.module.initializer.warning.structure.Warning;
import io.github.sakurawald.fuji.module.initializer.warning.structure.PlayerWarnings;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ListPlayerWarningsGui extends CrudPagedGui<Warning> {
    private final String targetPlayerName;

    public ListPlayerWarningsGui(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, String targetPlayerName, @NotNull List<Warning> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "warning.list.gui.title", targetPlayerName), entities, pageIndex);
        this.targetPlayerName = targetPlayerName;
    }

    @Override
    protected PagedGui<Warning> make(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<Warning> entities, int pageIndex) {
        return new ListPlayerWarningsGui(parent, player, this.targetPlayerName, entities, pageIndex);
    }

    public static ListPlayerWarningsGui make(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, @NotNull String targetPlayerName) {
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
            public void onClose() {
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
        return LuckpermsHelper.hasPermission(getPlayer().getUuid(), WarningInitializer.CREATE_WARNINGS_PERMISSION);
    }

    @Override
    protected boolean canReadEntity(Warning entity) {
        return LuckpermsHelper.hasPermission(getPlayer().getUuid(), WarningInitializer.READ_WARNINGS_PERMISSION);
    }

    @Override
    protected boolean canUpdateEntity(Warning entity) {
        return LuckpermsHelper.hasPermission(getPlayer().getUuid(), WarningInitializer.UPDATE_WARNINGS_PERMISSION);
    }

    @Override
    protected boolean canDeleteEntity(Warning entity) {
        return LuckpermsHelper.hasPermission(getPlayer().getUuid(), WarningInitializer.DELETE_WARNINGS_PERMISSION);
    }

    @Override
    protected void onLeftClickEntity(Warning entity) {
        if (!this.canUpdateEntity(entity)) {
            return;
        }

        String originalDescription = entity.getDescription();
        new EditSignGui(getPlayer(), originalDescription) {
            @Override
            public void onClose() {
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
