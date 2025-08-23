package io.github.sakurawald.fuji.module.initializer.command_advice.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandAdviceEntry {

    Matcher matcher = new Matcher();
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Matcher {

        @Document(id = 1751826314407L, value = """
        The `regex` expression used to match the `target command`.
        """)
        String commandStringRegex;

        @Document(id = 1751826318098L, value = """
        Is this `advice` only valid, when the target command is executed by a player?
        """)
        boolean executedByPlayerOnly;

    }

    @Document(id = 1751826320651L, value = """
        The type of this advice.
        """)
    CommandAdviceType adviceType;

    @Document(id = 1751826322556L, value = """
        The commands to execute when `perform` this `advice`.
        """)
    List<String> commands;
}
