package io.github.sakurawald.fuji.module.initializer.note.structure;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class PlayerNotes {

    public String player;

    public List<Note> notes = new ArrayList<>();

    public PlayerNotes(String player) {
        this.player = player;
    }
}
