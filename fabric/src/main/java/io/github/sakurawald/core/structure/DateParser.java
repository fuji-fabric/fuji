package io.github.sakurawald.core.structure;

import lombok.experimental.UtilityClass;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class DateParser {

    /* DSL definition */
    private static final int SECOND_TO_SECOND = 1;
    private static final int MINUTE_TO_SECOND = 60;
    private static final int HOUR_TO_SECOND = 60 * MINUTE_TO_SECOND;
    private static final int DAY_TO_SECOND = 24 * HOUR_TO_SECOND;
    private static final int WEEK_TO_SECOND = 7 * DAY_TO_SECOND;
    private static final int MONTH_TO_SECOND = 30 * DAY_TO_SECOND;
    private static final int YEAR_TO_SECOND = 12 * MONTH_TO_SECOND;
    private static final Pattern DATE_PARSER_DSL = Pattern.compile("(\\d+)([smhdwMy])");

    public static Date parseDate(String period) {
        /* compute the sum of seconds */
        Matcher matcher = DATE_PARSER_DSL.matcher(period);
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
                default:
                    throw new IllegalArgumentException("Unknown time unit: " + unit);
            }
        }

        /* add delta to now */
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, accumulateSeconds);
        return calendar.getTime();
    }
}
