package io.github.sakurawald.fuji.module.initializer.note.config.model;

import io.github.sakurawald.fuji.module.initializer.note.structure.PlayerNotes;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class NoteDataModel {

    public List<PlayerNotes> players = new ArrayList<>();

}
