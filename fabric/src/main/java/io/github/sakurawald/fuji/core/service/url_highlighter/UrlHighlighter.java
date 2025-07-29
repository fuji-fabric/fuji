package io.github.sakurawald.fuji.core.service.url_highlighter;

import java.util.regex.Pattern;

public class UrlHighlighter {
    private static final Pattern CLICKABLE_URL_REPLACER_PATTERN = Pattern.compile("(https?:\\/\\/(?:www\\.)?[a-zA-Z0-9\\-._~%]+(?:\\.[a-zA-Z]{2,})(?:[\\/?#][^\\s\"`]*)?)");

    public static String highlight(String string) {
        string = CLICKABLE_URL_REPLACER_PATTERN
            .matcher(string)
            .replaceAll("<click:open_url:'$1'><aqua><i><underline>$1<underline></></></>");
        return string;
    }
}
