package io.github.sakurawald.fuji.module.initializer.command_meta.json.command.argument.wrapper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import io.github.sakurawald.fuji.core.config.mapper.GsonMapper;

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
        return GsonMapper.fromJson(jsonValue, valueTransformer);
    }

}
