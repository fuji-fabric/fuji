package mod.fuji.module.initializer.command_cooldown.structure;

import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.structure.Cooldown;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
public class NamedCooldownDataNode {

    private static final String GLOBAL_DUMMY_NAME = "GLOBAL";

    String id;
    Cooldown<String> cooldown = new Cooldown<>();
    Map<String, Integer> uses = new HashMap<>();

    @ToString.Exclude
    transient NamedCooldownDescriptor descriptor;

    public static String toKey(@NotNull ServerPlayerEntity player) {
        return PlayerHelper.getPlayerName(player);
    }

    public long getRemainingTime(@NotNull String key, long cooldownPeriod) {
        return this.cooldown.getRemainingTime(getEffectiveKey(key), cooldownPeriod);
    }

    public long tryUse(@NotNull String key, long cooldownPeriod) {
        return this.cooldown.tryUse(getEffectiveKey(key), cooldownPeriod);
    }

    private String getEffectiveKey(@NotNull String key) {
        return this.getDescriptor().isGlobal() ? GLOBAL_DUMMY_NAME : key;
    }
}
