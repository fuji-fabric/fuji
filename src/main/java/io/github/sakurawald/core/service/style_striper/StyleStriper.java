package io.github.sakurawald.core.service.style_striper;

import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.PermissionHelper;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class StyleStriper {

    @SuppressWarnings("RegExpUnnecessaryNonCapturingGroup")
    private static final Pattern TAG_RESOLVER = Pattern.compile("<([^>]+)>");

    public static String stripe(PlayerEntity player, String type, String input) {
        for (String tag : resolveTags(input)) {
            String tagType = extractTagType(tag);
            if (!canUse(player, type, tagType)) {
                input = input.replace(tag, "");
            }
        }
        return input;
    }

    private static String extractTagType(String tag) {
        tag = tag.trim();

        if (tag.startsWith("/")) tag = tag.substring(1);

        // remove the escape character for vanilla minecraft sign.
        if (tag.endsWith("\\")) tag = tag.substring(0, tag.length() - 1);

        int colonIndex = tag.indexOf(':');
        if (colonIndex != -1) return tag.substring(0, colonIndex);

        int blankIndex = tag.indexOf(' ');
        if (blankIndex != -1) return tag.substring(0, blankIndex);

        return tag;
    }

    private static Set<String> resolveTags(String string) {
        /* extract tags */
        Set<String> tags = new HashSet<>();
        Matcher matcher = TAG_RESOLVER.matcher(string);
        while (matcher.find()) {
            String tag = matcher.group(1);
            tags.add(tag);
        }

        LogUtil.debug("resolve style tags: {}", tags);
        return tags;
    }

    private static boolean canUse(PlayerEntity player, String type, String tag) {
        String permission = "fuji.style.%s.%s".formatted(type, tag);
        return PermissionHelper.hasPermission(player.getUuid(), permission);
    }

}
