package mod.fuji.module.initializer.world.gamerule.config.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import mod.fuji.core.auxiliary.LogUtil;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.GameRules;

public class BooleanGameRuleMapAdapter implements JsonSerializer<Reference2BooleanMap<GameRules.Key<GameRules.BooleanRule>>>,
    JsonDeserializer<Reference2BooleanMap<GameRules.Key<GameRules.BooleanRule>>> {

    @Override
    public JsonElement serialize(Reference2BooleanMap<GameRules.Key<GameRules.BooleanRule>> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        for (GameRules.Key<GameRules.BooleanRule> key : src.keySet()) {
            String jsonKey = key.getName();
            boolean jsonValue = src.getBoolean(key);
            obj.addProperty(jsonKey, jsonValue);
        }
        return obj;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Reference2BooleanMap<GameRules.Key<GameRules.BooleanRule>> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        Reference2BooleanMap<GameRules.Key<GameRules.BooleanRule>> map = new Reference2BooleanOpenHashMap<>();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String jsonKey = entry.getKey();
            boolean jsonValue = entry.getValue().getAsBoolean();

            Optional<Map.Entry<GameRules.Key<?>, GameRules.Type<?>>> gameRuleEntryOptional = GameRules.RULE_TYPES
                .entrySet()
                .stream()
                .filter(it -> {
                    String gameRuleName = it.getKey().toString();
                    // NOTE: Ignore the gamerule case.
                    return gameRuleName.equalsIgnoreCase(jsonKey);
                })
                .findFirst();

            if (gameRuleEntryOptional.isEmpty()) {
                LogUtil.warn("Unknown game rule name {}, we will ignore it.", jsonKey);
                continue;
            }

            Map.Entry<GameRules.Key<?>, GameRules.Type<?>> gameRuleEntry = gameRuleEntryOptional.get();
            GameRules.Key<GameRules.BooleanRule> key = (GameRules.Key<GameRules.BooleanRule>) gameRuleEntry.getKey();
            map.put(key, jsonValue);
        }

        return map;
    }
}
