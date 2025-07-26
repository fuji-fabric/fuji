package io.github.sakurawald.fuji.core.auxiliary;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import org.jetbrains.annotations.Nullable;

public class ChronosUtil {

    private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

    public static @NotNull String getCurrentDate() {
        return getCurrentDate(DEFAULT_DATE_FORMAT);
    }

    public static @NotNull String getCurrentDate(SimpleDateFormat formatter) {
        return formatter.format(System.currentTimeMillis());
    }

    public static @NotNull String toDefaultDateFormat(@Nullable Long timeMillis) {
        if (timeMillis == null) {
            return "NONE";
        }

        return DEFAULT_DATE_FORMAT.format(timeMillis);
    }

    public static @NotNull Long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

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

    public static long toTimestamp(@NotNull ZonedDateTime zonedDateTime) {
        return zonedDateTime.toInstant().toEpochMilli();
    }
}
