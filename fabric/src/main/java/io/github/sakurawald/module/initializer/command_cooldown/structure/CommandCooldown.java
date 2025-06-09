package io.github.sakurawald.module.initializer.command_cooldown.structure;

import io.github.sakurawald.core.structure.Cooldown;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class CommandCooldown extends Cooldown<String> {

    private static final String GLOBAL_NAME = "GLOBAL";
    final @Nullable String name;
    final long cooldownMs;
    final int maxUsage;
    final boolean persistent;
    final boolean global;
    final Map<String, Integer> usage = new HashMap<>();

    @Override
    public long getCooldown(String key, Long cooldown) {
        return super.getCooldown(this.global ? GLOBAL_NAME : key, cooldown);
    }

    @Override
    public long tryUse(String key, Long cooldown) {
        return super.tryUse(this.global ? GLOBAL_NAME : key, cooldown);
    }
}
