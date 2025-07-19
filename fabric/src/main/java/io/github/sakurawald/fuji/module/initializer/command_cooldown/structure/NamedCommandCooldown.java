package io.github.sakurawald.fuji.module.initializer.command_cooldown.structure;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.structure.Cooldown;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
// NOTE: You need to provide a no args constructor for gson, or the field initializer will not be called.
@NoArgsConstructor
public class NamedCommandCooldown extends Cooldown<String> {

    private static final String GLOBAL_DUMMY_NAME = "GLOBAL";

    @Nullable String name;

    @SerializedName(value = "cooldown_duration", alternate = "cooldownMs")
    long cooldownDuration;

    @SerializedName(value = "max_uses", alternate = "maxUsage")
    int maxUses;

    boolean persistent;
    boolean global;

    @Document(id = 1752918450038L, value = """
        Defines the `success case commands` and `failure case commands` for `/command-cooldown try-use` command.
        """)
    TryUse tryUse = new TryUse();

    public static NamedCommandCooldown makeNamedCooldown(@Nullable String name, long cooldownDuration, int maxUses, boolean persistent, boolean global) {
        NamedCommandCooldown namedCommandCooldown = new NamedCommandCooldown();
        namedCommandCooldown.name = name;
        namedCommandCooldown.cooldownDuration = cooldownDuration;
        namedCommandCooldown.maxUses = maxUses;
        namedCommandCooldown.persistent = persistent;
        namedCommandCooldown.global = global;
        return namedCommandCooldown;
    }

    @Data
    public static class TryUse {
        @Document(id = 1752918599016L, value = """
            The commands to be executed if the `try-use` result of this named-cooldown is `success`.
            """)
        List<String> onSuccessCommands = new ArrayList<>() {
            {
                this.add("send-message %player:name% <green>Used once successfully.");
            }
        };
        @Document(id = 1752918620775L, value = """
            The commands to be executed if the `try-use` result of this named-cooldown is `failure`.
            """)
        List<String> onFailureCommands = new ArrayList<>() {
            {
                this.add("send-message %player:name% <red>Failed to use. %fuji:command_cooldown_left_time kitfood% and %fuji:command_cooldown_left_usage kitfood%");
            }
        };
    }

    @SerializedName(value = "uses", alternate = "usage")
    final Map<String, Integer> uses = new HashMap<>();

    @Override
    public long getRemainingTime(String key, Long cooldownPeriod) {
        return super.getRemainingTime(this.global ? GLOBAL_DUMMY_NAME : key, cooldownPeriod);
    }

    @Override
    public long tryUse(String key, Long cooldownPeriod) {
        return super.tryUse(this.global ? GLOBAL_DUMMY_NAME : key, cooldownPeriod);
    }

    public static String toKey(ServerPlayerEntity player) {
        return PlayerHelper.getPlayerName(player);
    }
}
