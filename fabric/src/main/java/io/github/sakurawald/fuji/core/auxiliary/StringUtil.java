package io.github.sakurawald.fuji.core.auxiliary;

import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;

public class StringUtil {

    public static String substituteGroupPlaceholders(@NotNull Matcher matcher, @NotNull String replacement) {
        replacement = matcher.replaceAll(replacement);
        matcher.reset();
        return replacement;
    }

    public static @NotNull String trimPathString(@NotNull String path) {
        return StringUtils.strip(path, ".");
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

    public static @NotNull String toLowerCase(@NotNull String string) {
        return string.toLowerCase(Locale.ENGLISH);
    }

    public static @NotNull String toUpperCase(@NotNull String string) {
        return string.toUpperCase(Locale.ENGLISH);
    }

    public static boolean containsIgnoreCase(@NotNull String string, @NotNull String keyword) {
        return toLowerCase(string)
            .contains(toLowerCase(keyword));
    }

    public static int levenshteinDistance(String str1, String str2) {
        int lenStr1 = str1.length();
        int lenStr2 = str2.length();
        int[][] dp = new int[lenStr1 + 1][lenStr2 + 1];

        for (int i = 0; i <= lenStr1; i++) {
            for (int j = 0; j <= lenStr2; j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j - 1] + (str1.charAt(i - 1) == str2.charAt(j - 1) ? 0 : 1),
                            dp[i - 1][j] + 1),
                            dp[i][j - 1] + 1);
                }
            }
        }
        return dp[lenStr1][lenStr2];
    }
}
