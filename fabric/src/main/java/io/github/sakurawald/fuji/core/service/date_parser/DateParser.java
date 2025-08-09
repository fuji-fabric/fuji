package io.github.sakurawald.fuji.core.service.date_parser;


import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DateParser {

    /* DSL definition. */
    private static final int SECOND_TO_SECOND = 1;
    private static final int MINUTE_TO_SECOND = 60;
    private static final int HOUR_TO_SECOND = 60 * MINUTE_TO_SECOND;
    private static final int DAY_TO_SECOND = 24 * HOUR_TO_SECOND;
    private static final int WEEK_TO_SECOND = 7 * DAY_TO_SECOND;
    private static final int MONTH_TO_SECOND = 30 * DAY_TO_SECOND;
    private static final int YEAR_TO_SECOND = 12 * MONTH_TO_SECOND;
    private static final Pattern DATE_PARSER_DSL = Pattern.compile("(\\d+)([smhdwMy])");

    public static @NotNull Date parseIntoExpirationDate(@NotNull String inputString) {
        /* Compute the sum of seconds. */
        int seconds = parseIntoSeconds(inputString);

        /* Add delta seconds to now. */
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.add(Calendar.SECOND, seconds);
        return nowCalendar.getTime();
    }

    public static @Nullable Long parseIntoExpirationTimestamp(@Nullable String inputString) {
        if (inputString == null) {
            return null;
        }

        return ChronosUtil.toTimestamp(parseIntoExpirationDate(inputString));
    }

    public static int parseIntoSeconds(@NotNull String inputString) {
        Matcher matcher = DATE_PARSER_DSL.matcher(inputString);
        int accumulateSeconds = 0;
        while (matcher.find()) {
            int quantity = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            switch (unit) {
                case "s":
                    accumulateSeconds += quantity * SECOND_TO_SECOND;
                    break;
                case "m":
                    accumulateSeconds += quantity * MINUTE_TO_SECOND;
                    break;
                case "h":
                    accumulateSeconds += quantity * HOUR_TO_SECOND;
                    break;
                case "d":
                    accumulateSeconds += quantity * DAY_TO_SECOND;
                    break;
                case "w":
                    accumulateSeconds += quantity * WEEK_TO_SECOND;
                    break;
                case "M":
                    accumulateSeconds += quantity * MONTH_TO_SECOND;
                    break;
                case "y":
                    accumulateSeconds += quantity * YEAR_TO_SECOND;
                    break;
            }
        }

        if (accumulateSeconds == 0) {
            throw new IllegalArgumentException("Invalid period: " + inputString);
        }
        return accumulateSeconds;
    }

    public static String formatSeconds(int totalSeconds) {
        if (totalSeconds < 0) throw new IllegalArgumentException("Seconds must be non-negative");

        StringBuilder result = new StringBuilder();

        int years = totalSeconds / YEAR_TO_SECOND;
        totalSeconds %= YEAR_TO_SECOND;
        if (years > 0) result.append(years).append("y");

        int months = totalSeconds / MONTH_TO_SECOND;
        totalSeconds %= MONTH_TO_SECOND;
        if (months > 0) result.append(months).append("M");

        int weeks = totalSeconds / WEEK_TO_SECOND;
        totalSeconds %= WEEK_TO_SECOND;
        if (weeks > 0) result.append(weeks).append("w");

        int days = totalSeconds / DAY_TO_SECOND;
        totalSeconds %= DAY_TO_SECOND;
        if (days > 0) result.append(days).append("d");

        int hours = totalSeconds / HOUR_TO_SECOND;
        totalSeconds %= HOUR_TO_SECOND;
        if (hours > 0) result.append(hours).append("h");

        int minutes = totalSeconds / MINUTE_TO_SECOND;
        totalSeconds %= MINUTE_TO_SECOND;
        if (minutes > 0) result.append(minutes).append("m");

        int seconds = totalSeconds; // remaining
        if (seconds > 0 || result.isEmpty()) {
            result.append(seconds).append("s");
        }

        return result.toString();
    }
}
