package io.github.sakurawald.core.structure;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.PermissionHelper;
import lombok.Data;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Data
public class Tag {

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
            String permission = "fuji.%s.%s".formatted(type, tag);
            if (PermissionHelper.hasPermission(player.getUuid(), permission)) {
                result = true;
                break;
            }
        }

        return result;
    }

}
