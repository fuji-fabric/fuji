package io.github.sakurawald.fuji.module.initializer.command_spy.config.model;

import io.github.sakurawald.fuji.module.initializer.command_spy.structure.CommandSpyRule;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommandSpyConfigModel {

    List<CommandSpyRule> rules = new ArrayList<>() {
        {
            this.add(
                new CommandSpyRule()
                    .setEnable(true)
                    .setMatcher(
                        new CommandSpyRule.Matcher()
                            .setCommandStringRegex(".+")
                            .setAcceptPlayerCommandSource(true)
                            .setAcceptServerCommandSource(false))
                    .setIfMatched(
                        new CommandSpyRule.IfMatched()
                            .setLogToConsole(true)));

        }
    };
}
