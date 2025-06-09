package io.github.sakurawald.module.initializer.command_meta.json.command.argument.wrapper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;

public enum JsonValueType {

    OBJECT(JsonObject.class),
    ARRAY(JsonArray.class),
    NULL(JsonNull.class),
    BOOLEAN(Boolean.class),
    INTEGER(Integer.class),
    FLOAT(Float.class),
    DOUBLE(Double.class),
    STRING(String.class);

    final Class<?> valueTransformer;

    JsonValueType(Class<?> valueTransformer) {
        this.valueTransformer = valueTransformer;
    }

    public Object parse(String jsonValue) {
        Gson gson = BaseConfigurationHandler.getGson();
        return gson.fromJson(jsonValue, valueTransformer);
    }

}
