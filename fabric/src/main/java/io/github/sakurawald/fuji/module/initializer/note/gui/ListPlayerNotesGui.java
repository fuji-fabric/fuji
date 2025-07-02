package io.github.sakurawald.fuji.module.initializer.note.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.gui.impl.gui.CrudPagedGui;
import io.github.sakurawald.fuji.core.gui.impl.gui.PagedGui;
import io.github.sakurawald.fuji.module.initializer.note.structure.Note;
import java.util.List;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ListPlayerNotesGui extends CrudPagedGui<Note> {

    public ListPlayerNotesGui(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, @NotNull List<Note> entities, int pageIndex) {
        super(parent, player, Text.literal("player Steve's warnings"), entities, pageIndex);
    }

    @Override
    protected PagedGui<Note> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<Note> entities, int pageIndex) {
        return new ListPlayerNotesGui(parent, player, entities, pageIndex);
    }

    public static ListPlayerNotesGui make(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, @NotNull String targetPlayerName) {
//        return new ListPlayerWarningsGui(parent, playerName);
        return null;
    }

    @Override
    protected GuiElementInterface toGuiElement(Note entity) {
        GuiElementBuilder builder = new GuiElementBuilder();

        builder
            .setItem(Items.PAPER);

        return builder.build();
    }

    @Override
    protected boolean filterEntity(Note entity, String keyword) {
        return false;
    }
}
