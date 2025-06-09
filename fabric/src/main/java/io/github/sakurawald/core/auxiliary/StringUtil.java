package io.github.sakurawald.core.auxiliary;

import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;

@UtilityClass
public class StringUtil {

    public static String replaceGroupsPlaceholders(Matcher matcher, String string) {
        for (int i = 0; i <= matcher.groupCount(); i++) {
            string = string.replace("$" + i, matcher.group(i));
        }

        return string;
    }
}
