package auxiliary;

import java.nio.file.Path;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class TestUtil {

    public static final Path ROOT_PROJECT_ROOT_PATH = Path.of("../");

    @SuppressWarnings("SameParameterValue")
    public static List<String> collectAllMatches(@NotNull Pattern pattern, @NotNull String inputString, int groupIndex) {
        Matcher matcher = pattern.matcher(inputString);

        List<String> collector = new ArrayList<>();
        while (matcher.find()) {
            collector.add(matcher.group(groupIndex));
        }

        return collector;
    }

}
