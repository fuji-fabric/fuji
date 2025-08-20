package io.github.sakurawald.fuji.module.initializer.world.gamerule.structure;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.world.GameRules;

@Data
@NoArgsConstructor
public class GameRuleDescriptor {
    public boolean enable = true;
    public String dimensionId;
    public GameRuleStore gameRules = GameRuleStore.makeDefaultGameRuleStore();

    public GameRuleDescriptor(String dimensionId) {
        this.dimensionId = dimensionId;
    }

    private transient GameRules vanillaGameRules;

    public GameRules asVanillaGameRules() {
        if (this.vanillaGameRules == null) {
            this.vanillaGameRules = GameRuleStore.makeGameRules();
            this.gameRules.applyTo(this.vanillaGameRules);
        }

        return this.vanillaGameRules;
    }

}
