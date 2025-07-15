package io.github.sakurawald.fuji.module.initializer.world.gamerule.structure;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import lombok.Data;
import net.minecraft.world.GameRules;

@Data
public class GameRuleDescriptor {
    public boolean enable = true;
    public final String dimensionId;
    public GameRuleStore gameRules = GameRuleStore.makeDefaultGameRuleStore();

    private transient GameRules vanillaGameRules;

    public GameRules asVanillaGameRules() {
        if (this.vanillaGameRules == null) {
            this.vanillaGameRules = GameRuleStore.makeGameRules();
            this.gameRules.applyTo(this.vanillaGameRules, ServerHelper.getServer());
        }

        return this.vanillaGameRules;
    }

}
