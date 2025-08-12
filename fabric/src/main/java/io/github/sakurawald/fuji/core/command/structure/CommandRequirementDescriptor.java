package io.github.sakurawald.fuji.core.command.structure;

import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.config.Configs;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("ClassCanBeRecord")
@Data
public class CommandRequirementDescriptor {

    final int level;
    final @Nullable String string;

    public CommandRequirementDescriptor(int level, @Nullable String string) {
        this.level = level;

        if (string != null && string.isBlank()) {
            this.string = null;
        } else this.string = string;
    }

    public static @Nullable CommandRequirementDescriptor from(@Nullable CommandRequirement annotation) {
        if (annotation == null) {
            return null;
        }
        return new CommandRequirementDescriptor(annotation.level(), annotation.string());
    }

    @SuppressWarnings("RedundantIfStatement")
    public static boolean isEmptyRequirement(@Nullable CommandRequirementDescriptor commandRequirement) {
        if (commandRequirement == null) {
            return true;
        }
        if (commandRequirement.level == 0 && commandRequirement.string == null) {
            return true;
        }

        return false;
    }

    public static int getInitialLevel() {
        return 0;
    }

    public static String getInitialString() {
        return null;
    }
}
