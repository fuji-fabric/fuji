package mod.fuji.core.service.style_striper;

import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import mod.fuji.core.document.annotation.DocStringProvider;
import mod.fuji.core.document.descriptor.PermissionDescriptor;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class StyleStriper {

    @SuppressWarnings("RegExpUnnecessaryNonCapturingGroup")
    private static final Pattern TAG_RESOLVER = Pattern.compile("<([^>]+)>");

    private static final Map<String, PermissionDescriptor> CREATED_STYLE_KINDS = new HashMap<>();

    public static @NotNull String stripe(@NotNull PlayerEntity player, @NotNull String styleKind, @NotNull String inputString) {
        for (String tag : resolveTags(inputString)) {
            String tagType = extractTagType(tag);
            if (!canUseThisTagType(player, styleKind, tagType)) {
                inputString = inputString.replace(tag, "");
            }
        }
        return inputString;
    }

    private static @NotNull String extractTagType(@NotNull String tag) {
        tag = tag.trim();

        if (tag.startsWith("/")) tag = tag.substring(1);

        // Remove the escape character for vanilla minecraft sign.
        if (tag.endsWith("\\")) tag = tag.substring(0, tag.length() - 1);

        int colonIndex = tag.indexOf(':');
        if (colonIndex != -1) return tag.substring(0, colonIndex);

        int blankIndex = tag.indexOf(' ');
        if (blankIndex != -1) return tag.substring(0, blankIndex);

        return tag;
    }

    private static @NotNull Set<String> resolveTags(@NotNull String string) {
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
        A player requires the `corresponding permission` to use that `style tag` in `a style kind`.

        For example:
        1. The `style kind` can be: `sign`, `anvil`...
        2. The `style tag` can be: `\\<yellow\\>`, `\\<bold\\>`...
        """)
    private static @NotNull PermissionDescriptor getOrCreatePermissionDescriptorForStyleKind(@NotNull String styleKind) {
        return CREATED_STYLE_KINDS.computeIfAbsent(styleKind, (key) -> {
            String pattern = "fuji.style.%s.<style-tag>";
            pattern = pattern.formatted(styleKind);
            return new PermissionDescriptor(pattern, 1751999453562L);
        });
    }

    private static boolean canUseThisTagType(@NotNull PlayerEntity player, @NotNull String styleKind, @NotNull String tagType) {
        PermissionDescriptor permission = getOrCreatePermissionDescriptorForStyleKind(styleKind);
        return LuckpermsHelper.hasPermission(player.getUuid(), permission, styleKind, tagType);
    }

}
