package io.github.sakurawald.fuji.module.initializer.world.structure.gamerule;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.NotNull;

@Document(id = 1752254462732L, value = """
    This structure is used to store the `game rules`.
    It stores both `boolean rules` and `int rules`.
    """)
public final class GameRuleStore {
    public Reference2BooleanMap<GameRules.Key<GameRules.BooleanRule>> booleanRules = new Reference2BooleanOpenHashMap<>();
    public Reference2IntMap<GameRules.Key<GameRules.IntRule>> intRules = new Reference2IntOpenHashMap<>();

    public void setBooleanRule(GameRules.Key<GameRules.BooleanRule> key, boolean value) {
        this.booleanRules.put(key, value);
    }

    public void setIntRule(GameRules.Key<GameRules.IntRule> key, int value) {
        this.intRules.put(key, value);
    }

    public boolean getBooleanRule(GameRules.Key<GameRules.BooleanRule> key) {
        return this.booleanRules.getBoolean(key);
    }

    public int getIntRule(GameRules.Key<GameRules.IntRule> key) {
        return this.intRules.getInt(key);
    }

    public boolean containsRule(GameRules.Key<?> key) {
        return this.booleanRules.containsKey(key) || this.intRules.containsKey(key);
    }

    public static GameRuleStore makeDefault() {
        GameRuleStore defaultGameRuleStore = new GameRuleStore();

        FeatureSet enabledFeatures = ServerHelper.getServer().getSaveProperties().getEnabledFeatures();
        GameRules gameRules = new GameRules(enabledFeatures);

        gameRules.accept(new GameRules.Visitor() {
            @SuppressWarnings("unchecked")
            @Override
            public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                String gameRuleName = key.getName();
                T gameRuleValue = gameRules.get(key);

                if (gameRuleValue.getClass().equals(GameRules.BooleanRule.class)) {
                    GameRules.Key<GameRules.BooleanRule> typedKey = (GameRules.Key<GameRules.BooleanRule>) key;
                    boolean typedValue = ((GameRules.BooleanRule) gameRuleValue).get();
                    defaultGameRuleStore.booleanRules.put(typedKey, typedValue);
                } else {
                    GameRules.Key<GameRules.IntRule> typedKey = (GameRules.Key<GameRules.IntRule>) key;
                    int typedValue = ((GameRules.IntRule) gameRuleValue).get();
                    defaultGameRuleStore.intRules.put(typedKey, typedValue);
                }

            }
        });

        return defaultGameRuleStore;
    }

    public void applyTo(GameRules rules, @NotNull MinecraftServer server) {
        Reference2BooleanMaps.fastForEach(this.booleanRules, entry -> {
            GameRules.BooleanRule rule = rules.get(entry.getKey());
            rule.set(entry.getBooleanValue(), server);
        });

        Reference2IntMaps.fastForEach(this.intRules, entry -> {
            GameRules.IntRule rule = rules.get(entry.getKey());
            rule.set(entry.getIntValue(), server);
        });
    }

}
