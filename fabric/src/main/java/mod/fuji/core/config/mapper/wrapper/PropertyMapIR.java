package mod.fuji.core.config.mapper.wrapper;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.lang.reflect.Type;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mod.fuji.core.auxiliary.minecraft.AuthlibHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyMapIR {

    Multimap<String, PropertyIR> properties;

    public static @NotNull PropertyMapIR fromNative(@NotNull PropertyMap vanilla) {
        Multimap<String, PropertyIR> map = HashMultimap.create();

        for (String key : vanilla.keySet().stream().toList()) {
            for (Property property : vanilla.get(key)) {
                map.put(key, PropertyIR.fromNative(property));
            }
        }

        return new PropertyMapIR(map);
    }

    public @NotNull PropertyMap toNative() {
        Multimap<String, Property> map = HashMultimap.create();

        if (properties != null) {
            for (var entry : properties.entries()) {
                map.put(entry.getKey(), entry.getValue().toNative());
            }
        }

        return AuthlibHelper.makePropertyMap(map);
    }


    /**
     * Implement the gson type adapter for wrapper type.
     **/
    public static class PropertyMapIRAdapter implements JsonSerializer<PropertyMapIR>, JsonDeserializer<PropertyMapIR> {

        private static final String NAME_KEY = "name";
        private static final String VALUE_KEY = "value";
        private static final String SIGNATURE_KEY = "signature";

        @Override
        public JsonElement serialize(@NotNull PropertyMapIR src, Type typeOfSrc, JsonSerializationContext context) {
            final JsonArray result = new JsonArray();

            /* Serialize the properties if exists. */
            if (src.getProperties() != null) {
                for (PropertyIR propertyIR : src.getProperties().values()) {
                    JsonObject propertyJsonObject = new JsonObject();

                    propertyJsonObject.addProperty(NAME_KEY, propertyIR.getName());
                    propertyJsonObject.addProperty(VALUE_KEY, propertyIR.getValue());
                    if (propertyIR.getSignature() != null) {
                        propertyJsonObject.addProperty(SIGNATURE_KEY, propertyIR.getSignature());
                    }

                    result.add(propertyJsonObject);
                }
            }

            return result;
        }

        @Override
        public PropertyMapIR deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            final Multimap<String, PropertyIR> map = HashMultimap.create();

            if (json.isJsonObject()) {
                JsonObject object = json.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    if (entry.getValue().isJsonArray()) {
                        for (JsonElement element : entry.getValue().getAsJsonArray()) {
                            map.put(entry.getKey(), parseProperty(entry.getKey(), element.getAsJsonObject()));
                        }
                    }
                }
            } else if (json.isJsonArray()) {
                for (JsonElement element : json.getAsJsonArray()) {
                    JsonObject obj = element.getAsJsonObject();
                    String name = obj.getAsJsonPrimitive(NAME_KEY).getAsString();
                    map.put(name, parseProperty(name, obj));
                }
            }

            return new PropertyMapIR(map);
        }

        private static @NotNull PropertyIR parseProperty(@NotNull String name, @NotNull JsonObject obj) {
            String value = obj.getAsJsonPrimitive(VALUE_KEY).getAsString();
            @Nullable String signature = obj.has(SIGNATURE_KEY) ? obj.getAsJsonPrimitive(SIGNATURE_KEY).getAsString() : null;
            return new PropertyIR(name, value, signature);
        }
    }
}
