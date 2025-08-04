package io.github.sakurawald.fuji.module.initializer.command_cooldown.structure;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Data
// NOTE: You need to provide a no args constructor for gson, or the field initializer will not be called.
@NoArgsConstructor
public class NamedCooldownDescriptor {

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

    public static NamedCooldownDescriptor make(@Nullable String name, long cooldownDuration, int maxUses, boolean persistent, boolean global) {
        NamedCooldownDescriptor namedCooldownDescriptor = new NamedCooldownDescriptor();
        namedCooldownDescriptor.name = name;
        namedCooldownDescriptor.cooldownDuration = cooldownDuration;
        namedCooldownDescriptor.maxUses = maxUses;
        namedCooldownDescriptor.persistent = persistent;
        namedCooldownDescriptor.global = global;
        return namedCooldownDescriptor;
    }

}
