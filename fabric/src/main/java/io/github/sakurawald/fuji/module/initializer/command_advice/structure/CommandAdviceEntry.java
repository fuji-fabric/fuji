package io.github.sakurawald.fuji.module.initializer.command_advice.structure;

import io.github.sakurawald.fuji.core.annotation.Document;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CommandAdviceEntry {

    @Document("""
        The `regex` expression used to match the `target command`.
        """)
    public String matchCommandStringRegex;

    @Document("""
        Is this `advice` only valid, when the target command is executed by a player?
        """)
    public boolean onlyValidWhenCommandIsExecutedByPlayer;

    @Document("""
        The type of this advice.
        """)
    public CommandAdviceType adviceType;

    @Document("""
        The commands to execute when `perform` this `advice`.
        """)
    public List<String> commands;
}
