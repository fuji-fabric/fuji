package io.github.sakurawald.core.command.structure;

import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.config.Configs;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("ClassCanBeRecord")
@Getter
public class CommandRequirementDescriptor {

    final int level;
    final @Nullable String string;

    public CommandRequirementDescriptor(int level, @Nullable String string) {
        this.level = level;

        if (string != null && string.isBlank()) {
            this.string = null;
        } else this.string = string;
    }

    /*
         1. This function will not work for BundleCommandDescriptor, since the gson will create the java object via the reflection.
         2. The command `/command-callback` is registered directly via the brigadier system.
         */
    public static @Nullable CommandRequirementDescriptor of(@Nullable CommandRequirement annotation) {
        /* override the default requirement */
        if (Configs.configHandler.model().core.permission.all_commands_require_level_4_permission_to_use_by_default) {
            return new CommandRequirementDescriptor(4, null);
        }

        /* primary */
        if (annotation == null) {
            return null;
        }

        return new CommandRequirementDescriptor(annotation.level(), annotation.string());
    }

    public static int getDefaultLevel() {
        return 0;
    }

    public static String getDefaultString() {
        return "";
    }
}
