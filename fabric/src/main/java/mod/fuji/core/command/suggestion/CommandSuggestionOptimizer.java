package mod.fuji.core.command.suggestion;

import mod.fuji.core.auxiliary.StringUtil;
import mod.fuji.core.command.suggestion.structure.CommandSuggestionCandidate;
import mod.fuji.core.document.annotation.ForDeveloper;
import mod.fuji.core.document.annotation.TestCase;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@ForDeveloper("""
    1. Regardless of the suggestion order sent by the server, the client will always display them in dictionary order.
    2. The CommandSuggestionProvider will be called when a new character is received from the client, but will not be called when the client press the `Tab` key.
    """)
@TestCase(action = "Issue `/when-online ...` and `/json put 1 2 3 ...` commands.", targets = "The command suggestion optimizer should work fine.")
public class CommandSuggestionOptimizer {

    public static <T> List<String> optimize(@NotNull Iterable<T> iterable, @NotNull String keyword) {
        /* Collect the candidates. */
        List<String> candidates = new ArrayList<>();
        for (T value : iterable) {
            candidates.add(value.toString());
        }

        /* Suggest all candidates if the keyword is blank. */
        if (keyword.isBlank()) {
            return candidates;
        }

        /* Map into pair. */
        return candidates
            .stream()
            .map(string -> {
                String stringLowerCase = StringUtil.toLowerCase(string);
                String keywordLowerCase = StringUtil.toLowerCase(keyword);
                int distance = 0;

                /* Use prefix matcher. */
                if (!stringLowerCase.startsWith(keywordLowerCase)) {
                    distance += 1000;
                }

                return new CommandSuggestionCandidate(string, distance);
            })
            .filter(it -> it.getScore() < 100)
            .map(CommandSuggestionCandidate::getSuggestion)
            .toList();
    }

}
