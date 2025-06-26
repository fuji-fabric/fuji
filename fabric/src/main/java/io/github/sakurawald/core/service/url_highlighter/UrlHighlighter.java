package io.github.sakurawald.core.service.url_highlighter;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class UrlHighlighter {
    private static final Pattern CLICKABLE_URL_REPLACER_PATTERN = Pattern.compile("(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})");

    public static String highlight(String string) {
        string = CLICKABLE_URL_REPLACER_PATTERN
            .matcher(string)
            .replaceAll("<click:open_url:'$1'><aqua><i>$1</i></aqua></click>");
        return string;
    }
}
