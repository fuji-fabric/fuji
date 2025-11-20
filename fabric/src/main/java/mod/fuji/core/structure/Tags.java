package mod.fuji.core.structure;

import mod.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import mod.fuji.core.document.annotation.DocStringProvider;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.document.descriptor.PermissionDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

@Document(id = 1756103268606L, value = """
    You can attach a named `tag` to an `entity`.
    Then reference this entity using the `tag` name.
    """)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Tags extends ArrayList<String> {

    public static Tags makeDefault() {
        Tags tags = new Tags();
        tags.add("default-tag-name");
        return tags;
    }

    private static final Map<String, PermissionDescriptor> CREATED_TAG_KIND_PERMISSIONS = new HashMap<>();

    @DocStringProvider(id = 1751998842298L, value = """
        Having this `permission`, means having the specified named `tag` for this `tag kind`.
        """)
    private static @NotNull PermissionDescriptor getOrCreateTagPermission(@NotNull String tagKind) {
        return CREATED_TAG_KIND_PERMISSIONS.computeIfAbsent(tagKind, k -> {
            String pattern = "fuji.%s.<tag-name>";
            pattern = pattern.formatted(tagKind);
            return new PermissionDescriptor(pattern, 1751998842298L);
        });
    }

    public static boolean hasAnyTagPermission(@NotNull Player player, String tagKind, Tags tags) {
        boolean result = false;
        for (String tag : tags) {
            PermissionDescriptor permission = getOrCreateTagPermission(tagKind);
            if (LuckpermsHelper.hasPermission(player.getUUID(), permission, tagKind, tag)) {
                result = true;
                break;
            }
        }

        return result;
    }

}
