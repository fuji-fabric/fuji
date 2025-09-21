package mod.fuji.core.command.suggestion.structure;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommandSuggestionCandidate {
    String suggestion;
    int score;
}
