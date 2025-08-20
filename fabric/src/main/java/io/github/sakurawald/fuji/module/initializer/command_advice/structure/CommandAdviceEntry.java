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

    @Document(id = 1751826314407L, value = """
        The `regex` expression used to match the `target command`.
        """)
    public String matchCommandStringRegex;

    @Document(id = 1751826318098L, value = """
        Is this `advice` only valid, when the target command is executed by a player?
        """)
    public boolean onlyValidWhenCommandIsExecutedByPlayer;

    @Document(id = 1751826320651L, value = """
        The type of this advice.
        """)
    public CommandAdviceType adviceType;

    @Document(id = 1751826322556L, value = """
        The commands to execute when `perform` this `advice`.
        """)
    public List<String> commands;
}
