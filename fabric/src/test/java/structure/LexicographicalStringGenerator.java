package structure;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfStringBuilder"})
@UtilityClass
public class LexicographicalStringGenerator {

    private static final StringBuilder output = new StringBuilder();

    public static void generate(int length) {
        char[] chars = new char[length];
        generate(chars, 0, length);
    }

    private static void generate(char @NotNull [] chars, int index, int length) {
        if (index == length) {
            String str = new String(chars);
            output.append("\"").append(str).append("\"").append(",");
            return;
        }

        for (char ch = 'a'; ch <= 'z'; ch++) {
            chars[index] = ch;
            generate(chars, index + 1, length);
        }
    }
}
