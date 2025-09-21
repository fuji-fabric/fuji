package mod.fuji.core.config.validator.structure;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@ToString
@EqualsAndHashCode
public class StringOccurenceMap {

    final Map<String, Integer> map = new HashMap<>();

    public void countOnce(@NotNull String string) {
        this.map.compute(string, (k, v) -> v == null ? 1 : v + 1);
    }

    private static @NotNull StringOccurenceMap makeStringOccurenceMap(@NotNull Pattern pattern, @NotNull String inputString) {
        StringOccurenceMap result = new StringOccurenceMap();
        Matcher matcher = pattern.matcher(inputString);
        while (matcher.find()) {
            String captureConversion = matcher.group(0);
            result.countOnce(captureConversion);
        }
        return result;
    }

    public static class JavaFormatterLanguage {
        private static final Pattern javaFormatterLanguage = Pattern.compile("(%[%bcdfsn])");
        public static @NotNull StringOccurenceMap makeOccurenceMap(@NotNull String inputString) {
            return makeStringOccurenceMap(javaFormatterLanguage, inputString);
        }

    }

    public static class NamedArgumentsLanguage {
        private static final Pattern textArgumentPlaceholderLanguage = Pattern.compile("(\\$\\{.+?})");
        public static @NotNull StringOccurenceMap makeOccurenceMap(@NotNull String inputString) {
            return makeStringOccurenceMap(textArgumentPlaceholderLanguage, inputString);
        }
    }
}
