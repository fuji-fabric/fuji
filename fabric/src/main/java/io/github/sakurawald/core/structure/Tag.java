package io.github.sakurawald.core.structure;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.PermissionHelper;
import io.github.sakurawald.core.structure.descriptor.PermissionDescriptor;
import lombok.Data;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Data
public class Tag {

    private static final PermissionDescriptor TAG_PERMISSION = new PermissionDescriptor("fuji.<type>.<tag-name>", "The permission used for `tags` on `specified type`.");

    @Document("""
        The tag names.
        """)
    List<String> tags = new ArrayList<>() {
        {
            this.add("default-tag-name");
        }
    };

    public static boolean hasAnyTagPermission(@NotNull PlayerEntity player, String type, List<String> tags) {
        boolean result = false;
        for (String tag : tags) {
            if (PermissionHelper.hasPermission(player.getUuid(), TAG_PERMISSION, type, tag)) {
                result = true;
                break;
            }
        }

        return result;
    }

}
