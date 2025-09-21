package mod.fuji.module.initializer.warning.structure;

import mod.fuji.core.document.annotation.Document;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
public class WarningRule {

    @Document(id = 1751827030101L, value = "If the `number of warnings` of the `player` >= `the defined value`, then execute the commands.")
    int IfNumberOfWarningsGreaterEqualThan;
    List<String> commands;

    public static WarningRule make(int ifNumberOfRulesGreaterEqualThan, @NotNull List<String> commands) {
        WarningRule warningRule = new WarningRule();
        warningRule.IfNumberOfWarningsGreaterEqualThan = ifNumberOfRulesGreaterEqualThan;
        warningRule.commands = commands;
        return warningRule;
    }

}
