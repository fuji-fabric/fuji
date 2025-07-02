package io.github.sakurawald.fuji.module.initializer.note.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.impl.gui.ConfirmSignGui;
import io.github.sakurawald.fuji.core.gui.impl.gui.CrudPagedGui;
import io.github.sakurawald.fuji.core.gui.impl.gui.EditSignGui;
import io.github.sakurawald.fuji.core.gui.impl.gui.InputSignGui;
import io.github.sakurawald.fuji.core.gui.impl.gui.PagedGui;
import io.github.sakurawald.fuji.module.initializer.note.NoteInitializer;
import io.github.sakurawald.fuji.module.initializer.note.structure.Note;
import io.github.sakurawald.fuji.module.initializer.note.structure.PlayerNotes;
import java.util.List;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ListPlayerNotesGui extends CrudPagedGui<Note> {
    private final String targetPlayerName;

    public ListPlayerNotesGui(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, String targetPlayerName, @NotNull List<Note> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "note.list.gui.title", targetPlayerName), entities, pageIndex);
        this.targetPlayerName = targetPlayerName;
    }

    @Override
    protected PagedGui<Note> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<Note> entities, int pageIndex) {
        return new ListPlayerNotesGui(parent, player, this.targetPlayerName, entities, pageIndex);
    }

    public static ListPlayerNotesGui make(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, @NotNull String targetPlayerName) {
        PlayerNotes playerNotes = NoteInitializer.withPlayerNotes(targetPlayerName);
        return new ListPlayerNotesGui(parent, player, targetPlayerName, playerNotes.notes, 0);
    }

    @Override
    protected GuiElementInterface toGuiElement(Note entity) {
        GuiElementBuilder builder = new GuiElementBuilder();

        builder
            .setItem(Items.PAPER)
            .setName(TextHelper.getTextByKey(getPlayer(), "note.list.gui.name"))
            .setLore(List.of(
                TextHelper.getTextByKey(getPlayer(), "entity.created_by_player", entity.createdByPlayer)
                , TextHelper.getTextByKey(getPlayer(), "entity.created_timestamp", ChronosUtil.toDefaultDateFormat(entity.createdTimestamp))
                , TextHelper.getTextByKey(getPlayer(),"entity.description", entity.description)
            ));

        builder.setCallback(dispatchClickType(getBackendGui(), entity));

        return builder.build();
    }

    @Override
    protected boolean filterEntity(Note entity, String keyword) {
        return false;
    }

    @Override
    protected @NotNull String getGuiHelpLoreKey() {
        return "note.list.gui.help.lore";
    }

    @Override
    protected void onCreateEntity() {
        new InputSignGui(getPlayer(), null) {
            @Override
            public void onClose() {
                String description = joinStrings();
                if (description.isBlank()) {
                    ListPlayerNotesGui.make(getParent(), player, targetPlayerName)
                        .open();
                    return;
                }

                PlayerNotes playerNotes = NoteInitializer.withPlayerNotes(targetPlayerName);
                Note note = Note.makeNote(player, description);
                playerNotes.notes.add(note);
                NoteInitializer.data.writeStorage();

                ListPlayerNotesGui.make(getParent(), player, targetPlayerName)
                    .open();
            }
        }.open();
    }

    @Override
    protected void onLeftClickEntity(Note entity) {
        String originalDescription = entity.description;

        new EditSignGui(getPlayer(), originalDescription) {
            @Override
            public void onClose() {
                String newDescription = joinStrings();
                entity.setDescription(newDescription);
                NoteInitializer.data.writeStorage();

                ListPlayerNotesGui.make(getParent(), player, targetPlayerName)
                    .open();
            }
        }.open();

    }

    @Override
    protected void onRightClickEntity(Note entity) {
        new ConfirmSignGui(getPlayer()) {
            @Override
            public void onConfirm() {
                NoteInitializer.withPlayerNotes(targetPlayerName)
                    .notes
                    .remove(entity);
                NoteInitializer.data.writeStorage();

                ListPlayerNotesGui.make(getParent(), getPlayer(),targetPlayerName)
                    .open();
            }
        }.open();
    }
}
