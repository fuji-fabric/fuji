package io.github.sakurawald.fuji.module.initializer.warning.service;

import io.github.sakurawald.fuji.module.initializer.warning.WarningInitializer;
import io.github.sakurawald.fuji.module.initializer.warning.structure.Warning;

public class WarningService {

    public static void createWarning(String creatorName, String targetPlayerName, String warningDescription) {
        /* Create a new warning for the target player. */
        Warning newWarning = Warning.makeWarning(creatorName, warningDescription);
        WarningInitializer
            .getPlayerWarnings(targetPlayerName)
            .warnings
            .add(newWarning);
        WarningInitializer.data.writeStorage();

        /* Process the warning rules. */
        WarningInitializer.processWarningRules(targetPlayerName);
    }

    public static void deleteWarning(String targetPlayerName, Warning warning) {
        WarningInitializer
            .getPlayerWarnings(targetPlayerName)
            .warnings
            .remove(warning);
        WarningInitializer.data.writeStorage();
    }

}
