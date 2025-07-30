package io.github.sakurawald.fuji.core.service.style_striper;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StyleStriper {

    @SuppressWarnings("RegExpUnnecessaryNonCapturingGroup")
    private static final Pattern TAG_RESOLVER = Pattern.compile("<([^>]+)>");

    private static final Map<String, PermissionDescriptor> CREATED_STYLE_TYPES = new HashMap<>();

    public static String stripe(PlayerEntity player, String type, String input) {
        for (String tag : resolveTags(input)) {
            String tagType = extractTagType(tag);
            if (!canUseThisTag(player, type, tagType)) {
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

        LogUtil.debug("Resolve style tags: {}", tags);
        return tags;
    }

    @DocStringProvider(id = 1751999453562L, value = """
        The permission used for `style tags striper`.
        A player requires the `corresponding permission` to use that `style tag` in `a specific style type`.
        """)
    private static PermissionDescriptor getOrCreatePermissionDescriptorForStyleType(String styleType) {
        return CREATED_STYLE_TYPES.computeIfAbsent(styleType, (it) -> {
            String pattern = "fuji.style.%s.<style-tag>";
            pattern = pattern.formatted(styleType);
            return new PermissionDescriptor(pattern, 1751999453562L);
        });
    }

    private static boolean canUseThisTag(PlayerEntity player, String type, String tag) {
        PermissionDescriptor permission = getOrCreatePermissionDescriptorForStyleType(type);
        return LuckpermsHelper.hasPermission(player.getUuid(), permission, type, tag);
    }

}
