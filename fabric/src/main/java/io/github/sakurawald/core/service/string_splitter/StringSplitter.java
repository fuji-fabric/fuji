package io.github.sakurawald.core.service.string_splitter;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class StringSplitter {
    private static final java.util.regex.Pattern STRING_SPLITTER_DSL = java.util.regex.Pattern.compile("(?<!\\\\)\\|");

    public static @NotNull List<String> split(@NotNull String input) {
        return Arrays.stream(STRING_SPLITTER_DSL.split(input)).toList();
    }
}
