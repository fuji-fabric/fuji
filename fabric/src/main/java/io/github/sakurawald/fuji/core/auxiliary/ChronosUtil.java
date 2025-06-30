package io.github.sakurawald.fuji.core.auxiliary;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;

public class ChronosUtil {

    private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

    public static @NotNull String getCurrentDate() {
        return getCurrentDate(DEFAULT_DATE_FORMAT);
    }

    public static @NotNull String getCurrentDate(SimpleDateFormat formatter) {
        return formatter.format(System.currentTimeMillis());
    }

    public static @NotNull String toDefaultDateFormat(long timeMillis) {
        return DEFAULT_DATE_FORMAT.format(timeMillis);
    }

    public static @NotNull Long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

}
