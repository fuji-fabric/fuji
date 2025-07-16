package io.github.sakurawald.fuji.core.service.duration_parser;

public class DurationParser {

    public static String formatDurationIntoCompact(long millis) {
        if (millis < 0) return "0ms";

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        seconds %= 60;
        minutes %= 60;
        hours %= 24;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d");
        if (hours > 0) sb.append(hours).append("h");
        if (minutes > 0) sb.append(minutes).append("m");
        if (seconds > 0 || sb.isEmpty())
            sb.append(seconds).append("s");

        return sb.toString();
    }

}
