package io.github.sakurawald.fuji.core.auxiliary;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;

public class StringUtil {

    public static String substituteGroupPlaceholders(@NotNull Matcher matcher, @NotNull String string) {
        for (int i = 0; i <= matcher.groupCount(); i++) {
            string = string.replace("$" + i, matcher.group(i));
        }

        return string;
    }

    public static @NotNull String formatBytes(long bytes) {
        if (bytes == -1) return "N/A";
        if (bytes < 1024) {
            return bytes + "B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2fK", (double) bytes / 1024);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2fM", (double) bytes / (1024 * 1024));
        } else {
            return String.format("%.2fG", (double) bytes / (1024 * 1024 * 1024));
        }
    }
}
