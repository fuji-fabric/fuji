package mod.fuji.core.command.structure;

import mod.fuji.core.command.annotation.CommandRequirement;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
public class CommandRequirementDescriptor {

    int level;
    @Nullable String string;

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
