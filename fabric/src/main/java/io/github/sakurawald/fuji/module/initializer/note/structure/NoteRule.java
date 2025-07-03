package io.github.sakurawald.fuji.module.initializer.note.structure;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import java.util.List;
import lombok.Data;

@Data
public class NoteRule {

    @Document("If the `number of notes` of the `player` >= `the defined value`, then execute the commands.")
    @SerializedName(value = "IfNumberOfNotesGreaterEqualThan", alternate = "IfNumberOfRulesGreaterEqualThan")
    public int IfNumberOfNotesGreaterEqualThan;
    public List<String> commands;

    public static NoteRule makeRule(int ifNumberOfRulesGreaterEqualThan, List<String> commands) {
        NoteRule noteRule = new NoteRule();
        noteRule.IfNumberOfNotesGreaterEqualThan = ifNumberOfRulesGreaterEqualThan;
        noteRule.commands = commands;
        return noteRule;
    }

}
