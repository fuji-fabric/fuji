package io.github.sakurawald.fuji.module.initializer.note.service;

import io.github.sakurawald.fuji.module.initializer.note.NoteInitializer;
import io.github.sakurawald.fuji.module.initializer.note.structure.Note;

public class NoteService {

    public static void createNote(String creatorName, String targetPlayerName, String noteDescription) {
        /* Create a new note for the target player. */
        Note newNote = Note.makeNote(creatorName, noteDescription);
        NoteInitializer
            .getPlayerNotes(targetPlayerName)
            .notes
            .add(newNote);
        NoteInitializer.data.writeStorage();

        /* Process the note rules. */
        NoteInitializer.processNoteRules(targetPlayerName);
    }

    public static void deleteNote(String targetPlayerName, Note note) {
        NoteInitializer
            .getPlayerNotes(targetPlayerName)
            .notes
            .remove(note);
        NoteInitializer.data.writeStorage();
    }

}
