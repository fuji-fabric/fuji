package io.github.sakurawald.fuji.module.initializer.note.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
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
        PlayerNotes playerNotes = NoteInitializer.getPlayerNotes(targetPlayerName);
        return new ListPlayerNotesGui(parent, player, targetPlayerName, playerNotes.notes, 0);
    }

    @Override
    protected GuiElementBuilder toGuiElementBuilder(Note entity) {
        GuiElementBuilder builder = new GuiElementBuilder();

        builder
            .setItem(Items.PAPER)
            .setName(TextHelper.getTextByKey(getPlayer(), "note.list.gui.name"))
            .setLore(entity.asLore(getPlayer()));

        return builder;
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

                PlayerNotes playerNotes = NoteInitializer.getPlayerNotes(targetPlayerName);
                Note note = Note.makeNote(player, description);
                playerNotes.notes.add(note);
                NoteInitializer.data.writeStorage();

                ListPlayerNotesGui.make(getParent(), player, targetPlayerName)
                    .open();
            }
        }.open();
    }

    @Override
    protected boolean canCreateEntity() {
        return LuckpermsHelper.hasPermission(getPlayer().getUuid(), NoteInitializer.CREATE_NOTES_PERMISSION);
    }

    @Override
    protected boolean canReadEntity(Note entity) {
        return LuckpermsHelper.hasPermission(getPlayer().getUuid(), NoteInitializer.READ_NOTES_PERMISSION);
    }

    @Override
    protected boolean canUpdateEntity(Note entity) {
        return LuckpermsHelper.hasPermission(getPlayer().getUuid(), NoteInitializer.UPDATE_NOTES_PERMISSION);
    }

    @Override
    protected boolean canDeleteEntity(Note entity) {
        return LuckpermsHelper.hasPermission(getPlayer().getUuid(), NoteInitializer.DELETE_NOTES_PERMISSION);
    }

    @Override
    protected void onLeftClickEntity(Note entity) {
        if (!this.canUpdateEntity(entity)) {
            return;
        }

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
        if (!canDeleteEntity(entity)) {
            return;
        }

        new ConfirmSignGui(getPlayer()) {
            @Override
            public void onConfirm() {
                NoteInitializer.getPlayerNotes(targetPlayerName)
                    .notes
                    .remove(entity);
                NoteInitializer.data.writeStorage();

                ListPlayerNotesGui.make(getParent(), getPlayer(),targetPlayerName)
                    .open();
            }
        }.open();
    }
}
