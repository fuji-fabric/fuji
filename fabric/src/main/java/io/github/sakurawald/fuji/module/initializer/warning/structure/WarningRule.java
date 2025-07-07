package io.github.sakurawald.fuji.module.initializer.warning.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import java.util.List;
import lombok.Data;

@Data
public class WarningRule {

    @Document(id = 1751827030101L, value = "If the `number of warnings` of the `player` >= `the defined value`, then execute the commands.")
    public int IfNumberOfWarningsGreaterEqualThan;
    public List<String> commands;

    public static WarningRule makeRule(int ifNumberOfRulesGreaterEqualThan, List<String> commands) {
        WarningRule warningRule = new WarningRule();
        warningRule.IfNumberOfWarningsGreaterEqualThan = ifNumberOfRulesGreaterEqualThan;
        warningRule.commands = commands;
        return warningRule;
    }

}
