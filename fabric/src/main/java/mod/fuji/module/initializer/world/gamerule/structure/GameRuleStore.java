package mod.fuji.module.initializer.world.gamerule.structure;

import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.document.annotation.Document;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.world.level.GameRules;

@Document(id = 1752254462732L, value = """
    This structure is used to store the `game rules`.
    It stores both `boolean rules` and `int rules`.
    """)
public final class GameRuleStore {
    public Reference2BooleanMap<GameRules.Key<GameRules.BooleanValue>> booleanRules = new Reference2BooleanOpenHashMap<>();
    public Reference2IntMap<GameRules.Key<GameRules.IntegerValue>> intRules = new Reference2IntOpenHashMap<>();

    public void setBooleanRule(GameRules.Key<GameRules.BooleanValue> key, boolean value) {
        this.booleanRules.put(key, value);
    }

    public void setIntRule(GameRules.Key<GameRules.IntegerValue> key, int value) {
        this.intRules.put(key, value);
    }

    public boolean getBooleanRule(GameRules.Key<GameRules.BooleanValue> key) {
        return this.booleanRules.getBoolean(key);
    }

    public int getIntRule(GameRules.Key<GameRules.IntegerValue> key) {
        return this.intRules.getInt(key);
    }

    public boolean containsRule(GameRules.Key<?> key) {
        return this.booleanRules.containsKey(key) || this.intRules.containsKey(key);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static GameRules makeGameRules() {
        #if MC_VER <= MC_1_21
        GameRules gameRules = new GameRules();
        #elif MC_VER > MC_1_21
        GameRules gameRules = new GameRules(ServerHelper.getServer().overworld().enabledFeatures());
        #endif

        return gameRules;
    }

    public static GameRuleStore makeDefaultGameRuleStore() {
        GameRuleStore defaultGameRuleStore = new GameRuleStore();

        defaultGameRuleStore.booleanRules.put(GameRules.RULE_KEEPINVENTORY, true);
        defaultGameRuleStore.intRules.put(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE, 50);
        return defaultGameRuleStore;
    }

    public void applyTo(GameRules rules) {
        Reference2BooleanMaps.fastForEach(this.booleanRules, entry -> {
            GameRules.BooleanValue rule = rules.getRule(entry.getKey());
            rule.set(entry.getBooleanValue(), ServerHelper.getServer());
        });

        Reference2IntMaps.fastForEach(this.intRules, entry -> {
            GameRules.IntegerValue rule = rules.getRule(entry.getKey());
            rule.set(entry.getIntValue(), ServerHelper.getServer());
        });
    }

}
