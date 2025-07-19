package io.github.sakurawald.fuji.module.initializer.command_cooldown.service;

import io.github.sakurawald.fuji.module.initializer.command_cooldown.CommandCooldownInitializer;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.structure.CommandCooldown;
import java.util.Map;

public class NamedCooldownService {

    public static Map<String, CommandCooldown> getNamedCooldownList() {
        return CommandCooldownInitializer.config.model().namedCooldown.list;
    }
}
