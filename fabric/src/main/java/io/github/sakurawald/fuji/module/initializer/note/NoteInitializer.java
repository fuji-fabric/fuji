package io.github.sakurawald.fuji.module.initializer.note;


import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.OfflinePlayerName;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import io.github.sakurawald.fuji.core.event.impl.PlayerEvents;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.note.config.model.NoteDataModel;
import io.github.sakurawald.fuji.module.initializer.note.gui.NoteGui;
import io.github.sakurawald.fuji.module.initializer.note.structure.Note;
import io.github.sakurawald.fuji.module.initializer.note.structure.PlayerNotes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;
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
        When a `player` with `notes` join/leave the server, you will get notified.
        """);

    public static final BaseConfigurationHandler<NoteDataModel> data = new ObjectConfigurationHandler<>("note-data.json", NoteDataModel.class);

    @Document("Open the note GUI.")
    @CommandNode("note")
    @CommandRequirement(level = 4)
    private static int $noteGui(@CommandSource ServerPlayerEntity player) {
        List<String> offlinePlayerNames = ServerHelper.getOfflinePlayerNames();
        new NoteGui(null, player, offlinePlayerNames, 0)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Create a new note for the player.")
    @CommandNode("note create")
    @CommandRequirement(level = 4)
    private static int $createNote(@CommandSource ServerCommandSource source, OfflinePlayerName targetPlayer, GreedyString note) {
        String creatorName = source.getName();
        String noteDescription = note.getValue();
        Note newNote = Note.makeNote(creatorName, noteDescription);

        String targetPlayerName = targetPlayer.getValue();
        NoteInitializer
            .getPlayerNotes(targetPlayerName)
            .notes
            .add(newNote);
        NoteInitializer.data.writeStorage();

        TextHelper.sendMessageByKey(source, "note.created", targetPlayerName);
        return CommandHelper.Return.SUCCESS;
    }

    @Document("List the notes of a player.")
    @CommandNode("note list")
    @CommandRequirement(level = 4)
    private static int $listNote(@CommandSource ServerCommandSource source, OfflinePlayerName targetPlayer) {
        String targetPlayerName = targetPlayer.getValue();
        PlayerNotes playerNotes = NoteInitializer.getPlayerNotes(targetPlayerName);
        TextHelper.sendMessageByKey(source, "note.list.message", targetPlayerName, playerNotes.notes.size());

        playerNotes.notes.forEach(note -> {
            note
                .asLore(source)
                .forEach(source::sendMessage);

            source.sendMessage(TextHelper.TEXT_EMPTY);
        });

        return CommandHelper.Return.SUCCESS;
    }

    @Document("Clear the notes of a player.")
    @CommandNode("note clear")
    @CommandRequirement(level = 4)
    private static int $clearNote(@CommandSource ServerCommandSource source, OfflinePlayerName targetPlayer) {
        String targetPlayerName = targetPlayer.getValue();
        List<Note> notes = NoteInitializer.getPlayerNotes(targetPlayerName).notes;
        int originalSize = notes.size();
        notes.clear();
        NoteInitializer.data.writeStorage();

        TextHelper.sendMessageByKey(source, "note.clear", originalSize, targetPlayerName);
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Clear all notes for all players.")
    @CommandNode("note clear-all")
    @CommandRequirement(level = 4)
    private static int $clearAllNote(@CommandSource ServerCommandSource source, Optional<Boolean> confirm) {
        Boolean confirmed = confirm.orElse(false);
        if (!confirmed) {
            TextHelper.sendMessageByKey(source, "operation.cancelled");
            return CommandHelper.Return.SUCCESS;
        }

        NoteInitializer.data.model().players = new ArrayList<>();
        NoteInitializer.data.writeStorage();

        TextHelper.sendMessageByKey(source, "note.clear_all");
        return CommandHelper.Return.SUCCESS;
    }

    public static PlayerNotes getPlayerNotes(String playerName) {
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

    @Override
    protected void onInitialize() {
        PlayerEvents.ON_PLAYER_JOINED.register(player -> processNotify(player, true));
        PlayerEvents.ON_PLAYER_LEAVE.register(player -> processNotify(player, false));
    }

    public static void processNotify(ServerPlayerEntity targetPlayer, boolean isJoin) {
        /* Does the player have any notes? */
        String playerName = PlayerHelper.getPlayerName(targetPlayer);
        PlayerNotes playerNotes = getPlayerNotes(playerName);
        if (playerNotes.notes.isEmpty()) return;

        /* Send notify to online staffs. */
        ServerHelper
            .getOnlinePlayers()
            .stream()
            .filter(it -> LuckpermsHelper.hasPermission(it.getUuid(), NOTIFY_NOTES_PERMISSION))
            .forEach(it -> {
                int notesSize = playerNotes.notes.size();
                if (isJoin) {
                    TextHelper.sendMessageByKey(it, "note.notify.join", playerName, notesSize);
                } else {
                    TextHelper.sendMessageByKey(it, "note.notify.leave", playerName, notesSize);
                }
            });
    }
}
