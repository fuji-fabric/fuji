package io.github.sakurawald.fuji.module.initializer.note;


import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.note.config.model.NoteDataModel;
import io.github.sakurawald.fuji.module.initializer.note.gui.NoteGui;
import io.github.sakurawald.fuji.module.initializer.note.structure.PlayerNotes;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;

@Document("""
    This module provides the `information management` for `staffs`.
    You can `create` a `note/warning` for a `player`.
    All `staffs` can `view` the `notes/warnings` of a `player`.

    You can use `notes/warnings` to `track` the behaviours of a `player`.
    """)
public class NoteInitializer extends ModuleInitializer {


    public static PermissionDescriptor CREATE_NOTES_PERMISSION = new PermissionDescriptor("fuji.note.create", """
        To `create` a new `note` for a `player`.
        """);

    public static PermissionDescriptor READ_NOTES_PERMISSION = new PermissionDescriptor("fuji.note.read", """
        To `read` the `notes` of a `player`.
        """);

    public static PermissionDescriptor UPDATE_NOTES_PERMISSION = new PermissionDescriptor("fuji.note.update", """
        To `update` the `notes` of a `player`.
        """);

    public static PermissionDescriptor DELETE_NOTES_PERMISSION = new PermissionDescriptor("fuji.note.delete", """
        To `delete` an existed `note` of a `player`.
        """);

    public static PermissionDescriptor NOTIFY_NOTES_PERMISSION = new PermissionDescriptor("fuji.note.notify", """
        When a `player` with `notes` join the server, you will get notified.
        """);

    public static final BaseConfigurationHandler<NoteDataModel> data = new ObjectConfigurationHandler<>("note-data.json", NoteDataModel.class);

    @Document("Open the note GUI.")
    @CommandNode("note")
    @CommandRequirement(level = 4)
    private static int $note(@CommandSource ServerPlayerEntity player) {
        List<String> offlinePlayerNames = ServerHelper.getOfflinePlayerNames();
        new NoteGui(null, player, offlinePlayerNames, 0)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    public static PlayerNotes withPlayerNotes(String playerName) {
        /* Return existed player notes. */
        List<PlayerNotes> players = data.model().players;
        Optional<PlayerNotes> playerNotesOpt = players
            .stream()
            .filter(it -> it.player.equals(playerName))
            .findFirst();
        if (playerNotesOpt.isPresent()) {
            return playerNotesOpt.get();
        }

        /* Make a new one. */
        PlayerNotes playerNotes = new PlayerNotes(playerName);
        players.add(playerNotes);
        data.writeStorage();
        return playerNotes;
    }
}
