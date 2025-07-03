package io.github.sakurawald.fuji.module.initializer.note.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.note.structure.NoteRule;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class NoteConfigModel {

    @Document("""
        Define `note rules`, to execute `punishment commands`.

        When a new `note` is `added` to a player, we will process the `note rules`.
        And then pick up `one note rule` to execute its commands.
        We will pick the `highest` number of notes satisfied first.

        """)
    public List<NoteRule> rules = new ArrayList<>() {
        {
            this.add(NoteRule.makeRule(1, List.of(
                "when-online %player:name% send-message %player:name% <red>You are warned. Watch your behaviour!")));
            this.add(NoteRule.makeRule(3, List.of(
                "temp-ban player %player:name% 30m Noted 3 times.")));

        }
    };

}
