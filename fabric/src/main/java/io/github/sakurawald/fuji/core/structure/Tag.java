package io.github.sakurawald.fuji.core.structure;

import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class Tag {

    private static final Map<String, PermissionDescriptor> CREATED_TAG_PERMISSIONS = new HashMap<>();

    @Document(id = 1751823907693L, value = """
        Attached `tag name` list for this `configuration section`.
        """)
    public List<String> tags = new ArrayList<>() {
        {
            this.add("default-tag-name");
        }
    };

    @DocStringProvider(id = 1751998842298L, value = """
        Having this `permission`, means having the specified `tag name` for specified `tag type`.
        """)
    private static PermissionDescriptor getOrCreateTagPermission(String tagType) {
        return CREATED_TAG_PERMISSIONS.computeIfAbsent(tagType, k -> {
            String pattern = "fuji.%s.<tag-name>";
            pattern = pattern.formatted(tagType);
            return new PermissionDescriptor(pattern, 1751998842298L);
        });
    }

    public static boolean hasAnyTagPermission(@NotNull PlayerEntity player, String tagType, List<String> tagNames) {
        boolean result = false;
        for (String tag : tagNames) {
            PermissionDescriptor permission = getOrCreateTagPermission(tagType);
            if (LuckpermsHelper.hasPermission(player.getUuid(), permission, tagType, tag)) {
                result = true;
                break;
            }
        }

        return result;
    }

}
