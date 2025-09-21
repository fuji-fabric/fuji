package mod.fuji.module.initializer.world.gamerule.config.model;

import mod.fuji.module.initializer.world.gamerule.structure.GameRuleDescriptor;
import java.util.ArrayList;
import java.util.List;

public class WorldGameRuleConfigModel {

    public List<GameRuleDescriptor> gameRules = new ArrayList<>() {
        {
            this.add(new GameRuleDescriptor("fuji:example"));
        }
    };
}
