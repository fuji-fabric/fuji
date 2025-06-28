package io.github.sakurawald.fuji.core.auxiliary;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;

@UtilityClass
public class ChronosUtil {

    private static final SimpleDateFormat STANDARD_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

    public static @NotNull String getCurrentDate() {
        return getCurrentDate(STANDARD_DATE_FORMAT);
    }

    public static @NotNull String getCurrentDate(SimpleDateFormat formatter) {
        return formatter.format(System.currentTimeMillis());
    }

    public static @NotNull String toStandardDateFormat(long timeMillis) {
        return STANDARD_DATE_FORMAT.format(timeMillis);
    }

    public static @NotNull Long getCurrentMillis() {
        return System.currentTimeMillis();
    }

}
