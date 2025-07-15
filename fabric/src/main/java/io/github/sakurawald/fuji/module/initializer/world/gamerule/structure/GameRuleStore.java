package io.github.sakurawald.fuji.module.initializer.world.gamerule.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import it.unimi.dsi.fastutil.objects.*;
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

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static GameRules makeGameRules() {
        #if MC_VER <= MC_1_21
        GameRules gameRules = new GameRules();
        #elif MC_VER > MC_1_21
        GameRules gameRules = new GameRules(ServerHelper.getServer().getOverworld().getEnabledFeatures());
        #endif

        return gameRules;
    }

    public static GameRuleStore makeDefaultGameRuleStore() {
        GameRuleStore defaultGameRuleStore = new GameRuleStore();

        defaultGameRuleStore.booleanRules.put(GameRules.KEEP_INVENTORY, true);
        defaultGameRuleStore.intRules.put(GameRules.PLAYERS_SLEEPING_PERCENTAGE, 50);
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
