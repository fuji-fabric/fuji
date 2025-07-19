package io.github.sakurawald.fuji.module.initializer.command_cooldown.structure;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.structure.Cooldown;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NamedCommandCooldown extends Cooldown<String> {

    private static final String GLOBAL_NAME = "GLOBAL";

    final @Nullable String name;

    @SerializedName(value = "cooldown_duration", alternate = "cooldownMs")
    final long cooldownDuration;

    @SerializedName(value = "max_uses", alternate = "maxUsage")
    final int maxUses;

    final boolean persistent;
    final boolean global;

    @SerializedName(value = "uses", alternate = "usage")
    final Map<String, Integer> uses = new HashMap<>();

    @Override
    public long getRemainingTime(String key, Long cooldownPeriod) {
        return super.getRemainingTime(this.global ? GLOBAL_NAME : key, cooldownPeriod);
    }

    @Override
    public long tryUse(String key, Long cooldownPeriod) {
        return super.tryUse(this.global ? GLOBAL_NAME : key, cooldownPeriod);
    }
}
