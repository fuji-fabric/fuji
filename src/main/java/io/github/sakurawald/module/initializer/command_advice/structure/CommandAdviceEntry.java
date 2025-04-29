package io.github.sakurawald.module.initializer.command_advice.structure;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CommandAdviceEntry {

    public String matchCommandStringRegex;
    public boolean onlyValidWhenCommandIsExecutedByPlayer;
    public CommandAdviceType adviceType;
    public List<String> commands;
}
