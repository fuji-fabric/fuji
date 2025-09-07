package io.github.sakurawald.fuji.core.auxiliary;

import io.github.sakurawald.fuji.core.config.Configs;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;

public class ChronosUtil {

    public static @NotNull ZoneId getPreferredZoneId() {
        return ZoneId.of("UTC");
    }

    public static @NotNull ZonedDateTime getZonedDateTime() {
        return ZonedDateTime.now(getPreferredZoneId());
    }

    public static @NotNull LocalDate getLocalDate() {
        return LocalDate.now(getPreferredZoneId());
    }

    public static @NotNull LocalTime getLocalTime() {
        return LocalTime.now(getPreferredZoneId());
    }

    public static @NotNull Long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    public static long toTimestamp(@NotNull ZonedDateTime zonedDateTime) {
        return zonedDateTime.toInstant().toEpochMilli();
    }

    public static long toTimestamp(@NotNull Date date) {
        return date.toInstant().toEpochMilli();
    }

    public static class Formatter {
        private static final String FALLBACK_DATE_FORMATTER_PATTERN = "yyyy-MM-dd HH:mm:ss";
        private static final DateTimeFormatter FALLBACK_DATE_FORMATTER = DateTimeFormatter.ofPattern(FALLBACK_DATE_FORMATTER_PATTERN);
        private static String effectiveDateFormatterPattern = null;
        private static DateTimeFormatter effectiveDateFormatter = null;

        public static DateTimeFormatter getEffectiveDateFormatter() {
            /* Cache for effective formatter variable. */
            String specifiedDateFormatterPattern = Configs.MAIN_CONTROL_CONFIG.model().core.formatter.date_formatter;
            if (effectiveDateFormatterPattern == null
                || (!effectiveDateFormatterPattern.equals(specifiedDateFormatterPattern) && !effectiveDateFormatterPattern.equals(FALLBACK_DATE_FORMATTER_PATTERN))) {
                try {
                    effectiveDateFormatter = DateTimeFormatter.ofPattern(specifiedDateFormatterPattern);
                    effectiveDateFormatterPattern = specifiedDateFormatterPattern;
                } catch (Exception e) {
                    LogUtil.error("""
                    Failed to parse the specified date formatter pattern {}.
                    Falling back to the default pattern {}.
                    You can read the syntax of date formatter in: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
                    """, specifiedDateFormatterPattern, FALLBACK_DATE_FORMATTER_PATTERN);
                    effectiveDateFormatter = FALLBACK_DATE_FORMATTER;
                    effectiveDateFormatterPattern = FALLBACK_DATE_FORMATTER_PATTERN;
                }
            }

            /* Return the value of this variable. */
            return effectiveDateFormatter;
        }

        public static @NotNull String getFormattedCurrentDate() {
            DateTimeFormatter formatter = getEffectiveDateFormatter();
            return getFormattedCurrentDate(formatter);
        }

        public static @NotNull String getFormattedCurrentDate(DateTimeFormatter formatter) {
            return formatDate(formatter, System.currentTimeMillis());
        }

        public static @NotNull String formatDate(@Nullable Long timeMillis) {
            return formatDate(getEffectiveDateFormatter(), timeMillis);
        }

        public static @NotNull String formatDate(@NotNull DateTimeFormatter formatter, @Nullable Long timeMillis) {
            if (timeMillis == null) {
                return "NONE";
            }

            ZonedDateTime zonedDateTime = Instant.ofEpochMilli(timeMillis)
                .atZone(ZoneId.systemDefault());

            return zonedDateTime.format(formatter);
        }
    }

    public static class Boundary {

        public static @NotNull ZonedDateTime getBeginningOfCurrentYear(ZonedDateTime now) {
            return now.withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);
        }

        public static @NotNull ZonedDateTime getBeginningOfCurrentMonth(ZonedDateTime now) {
            return now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        }

        public static @NotNull ZonedDateTime getBeginningOfCurrentWeek(ZonedDateTime now, DayOfWeek dayOfWeek) {
            return now.with(TemporalAdjusters.previousOrSame(dayOfWeek)).truncatedTo(ChronoUnit.DAYS);
        }

        public static @NotNull ZonedDateTime getBeginningOfCurrentHour(ZonedDateTime now) {
            return now.truncatedTo(ChronoUnit.HOURS);
        }

        public static @NotNull ZonedDateTime getBeginningOfTheDay(ZonedDateTime now) {
            return now.truncatedTo(ChronoUnit.DAYS);
        }
    }
}
