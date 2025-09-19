package io.github.sakurawald.fuji.module.initializer.document.formatter;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class MarkdownDocumentFormatter {

    public static @NotNull String compile(@NotNull String input) {
        return Arrays
            .stream(input.split("\n"))
            .map(MarkdownDocumentFormatter::compileLine)
            .collect(Collectors.joining("\n"));
    }

    private static @NotNull String compileLine(@NotNull String input) {
        String output = input;

        /* Stripe the named color tags. */
        for (String namedColor : TextHelper.Formatter.NAMED_STYLE_TAGS) {
            output = output.replaceAll("<\\/?" + namedColor + ">","");
        }

        /* Stripe the hex color tags. */
        output = output.replaceAll("<\\/?#[a-zA-Z0-9_]+>", "");

        /* Un-escape the escaped tags inside code-fence. */
        output = unescapeTagsWithinInlineCodeFence(output);

        return output;
    }

    private static @NotNull String unescapeTagsWithinInlineCodeFence(@NotNull String input) {
        /* Match inline code spans: `...` */
        Pattern codePattern = Pattern.compile("`([^`]+)`");
        Matcher matcher = codePattern.matcher(input);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String codeBlock = matcher.group(1);

            // Unescape < and >
            String unescaped = codeBlock.replaceAll("\\\\([<>])", "$1");

            // Put backticks around it
            matcher.appendReplacement(result, "`" + Matcher.quoteReplacement(unescaped) + "`");
        }
        matcher.appendTail(result);

        return result.toString();
    }

}
