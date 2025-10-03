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
import mod.fuji.core.auxiliary.minecraft.AuthlibHelper;
import java.lang.reflect.Type;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyMapWrapper {

     Multimap<String, PropertyWrapper> properties;

     public static @NotNull PropertyMapWrapper fromVanillaType(@NotNull PropertyMap vanilla) {
        Multimap<String, PropertyWrapper> map = HashMultimap.create();

        for (String key : vanilla.keySet().stream().toList()) {
            for (Property property : vanilla.get(key)) {
                map.put(key, PropertyWrapper.fromVanillaType(property));
            }
        }

        return new PropertyMapWrapper(map);
    }

    public @NotNull PropertyMap toVanillaType() {
        Multimap<String, Property> map = HashMultimap.create();

        if (properties != null) {
            for (var entry : properties.entries()) {
                map.put(entry.getKey(), entry.getValue().toVanillaType());
            }
        }

        return AuthlibHelper.makePropertyMap(map);
    }


    /**
 * Implement the gson type adapter for wrapper type.
 **/
    public static class PropertyMapWrapperAdapter implements JsonSerializer<PropertyMapWrapper>, JsonDeserializer<PropertyMapWrapper> {

        private static final String NAME_KEY = "name";
        private static final String VALUE_KEY = "value";
        private static final String SIGNATURE_KEY = "signature";

        @Override
        public JsonElement serialize(@NotNull PropertyMapWrapper src, Type typeOfSrc, JsonSerializationContext context) {
            final JsonArray result = new JsonArray();

            if (src.getProperties() != null) {
                for (PropertyWrapper wrapper : src.getProperties().values()) {
                    JsonObject object = new JsonObject();
                    object.addProperty(NAME_KEY, wrapper.getName());
                    object.addProperty(VALUE_KEY, wrapper.getValue());

                    if (wrapper.getSignature() != null) {
                        object.addProperty(SIGNATURE_KEY, wrapper.getSignature());
                    }

                    result.add(object);
                }
            }

            return result;
        }

        @Override
        public PropertyMapWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            final Multimap<String, PropertyWrapper> map = HashMultimap.create();

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

            return new PropertyMapWrapper(map);
        }

        private static @NotNull PropertyWrapper parseProperty(String name, JsonObject obj) {
            String value = obj.getAsJsonPrimitive(VALUE_KEY).getAsString();
            String signature = obj.has(SIGNATURE_KEY) ? obj.getAsJsonPrimitive(SIGNATURE_KEY).getAsString() : null;
            return new PropertyWrapper(name, value, signature);
        }
    }
}
