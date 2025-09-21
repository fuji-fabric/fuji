package mod.fuji.module.initializer.world.gamerule.config.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import mod.fuji.core.auxiliary.LogUtil;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.GameRules;

public class IntegerGameRuleMapAdapter implements JsonSerializer<Reference2IntMap<GameRules.Key<GameRules.IntRule>>>,
    JsonDeserializer<Reference2IntMap<GameRules.Key<GameRules.IntRule>>> {

    @Override
    public JsonElement serialize(Reference2IntMap<GameRules.Key<GameRules.IntRule>> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        for (GameRules.Key<GameRules.IntRule> key : src.keySet()) {
            String jsonKey = key.getName();
            int jsonValue = src.getInt(key);
            obj.addProperty(jsonKey, jsonValue);
        }
        return obj;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Reference2IntMap<GameRules.Key<GameRules.IntRule>> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        Reference2IntMap<GameRules.Key<GameRules.IntRule>> map = new Reference2IntOpenHashMap<>();

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String jsonKey = entry.getKey();
            int jsonValue = entry.getValue().getAsInt();

            Optional<Map.Entry<GameRules.Key<?>, GameRules.Type<?>>> gameRuleEntryOptional = GameRules.RULE_TYPES
                .entrySet()
                .stream()
                .filter(it -> {
                    String gameRuleName = it.getKey().toString();
                    return gameRuleName.equalsIgnoreCase(jsonKey);
                })
                .findFirst();

            if (gameRuleEntryOptional.isEmpty()) {
                LogUtil.warn("Unknown game rule name {}, we will ignore it.", jsonKey);
                continue;
            }

            Map.Entry<GameRules.Key<?>, GameRules.Type<?>> gameRuleEntry = gameRuleEntryOptional.get();
            GameRules.Key<GameRules.IntRule> key = (GameRules.Key<GameRules.IntRule>) gameRuleEntry.getKey();
            map.put(key, jsonValue);
        }

        return map;
    }
}
