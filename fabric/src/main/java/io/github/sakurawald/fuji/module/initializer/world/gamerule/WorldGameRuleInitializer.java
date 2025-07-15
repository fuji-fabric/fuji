package io.github.sakurawald.fuji.module.initializer.world.gamerule;

import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.world.gamerule.config.model.WorldGameRuleConfigModel;
import io.github.sakurawald.fuji.module.initializer.world.gamerule.structure.BooleanGameRuleMapAdapter;
import io.github.sakurawald.fuji.module.initializer.world.gamerule.structure.GameRuleDescriptor;
import io.github.sakurawald.fuji.module.initializer.world.gamerule.structure.IntegerGameRuleMapAdapter;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.util.Optional;

@Document(id = 1752577892546L, value = """
    This module allows you to customize the `per-dimension gamerule`.
    """)
public class WorldGameRuleInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<WorldGameRuleConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, WorldGameRuleConfigModel.class);


    public static Optional<GameRuleDescriptor> getEffectiveGameRuleDescriptor(String dimensionId) {
        return config.model().gameRules
            .stream()
            .filter(it -> it.enable
                && it.dimensionId.equals(dimensionId))
            .findFirst();
    }

    @Override
    protected void registerGsonTypeAdapter() {
        BaseConfigurationHandler.registerGsonTypeAdapter(Reference2BooleanMap.class, new BooleanGameRuleMapAdapter());
        BaseConfigurationHandler.registerGsonTypeAdapter(Reference2IntMap.class, new IntegerGameRuleMapAdapter());
    }
}
