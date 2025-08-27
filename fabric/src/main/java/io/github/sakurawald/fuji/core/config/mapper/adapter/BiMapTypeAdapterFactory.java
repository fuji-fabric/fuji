package io.github.sakurawald.fuji.core.config.mapper.adapter;

import com.google.common.collect.BiMap;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class BiMapTypeAdapterFactory implements TypeAdapterFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        // Check if the type is a BiMap
        if (!BiMap.class.isAssignableFrom(type.getRawType())) {
            return null; // not a BiMap, let Gson handle it
        }

        // Figure out the key/value types
        Type[] typeArgs = ((ParameterizedType) type.getType()).getActualTypeArguments();
        Type valueType = typeArgs[1];

        // Ask Gson for normal adapters for key/value
        TypeAdapter<?> valueAdapter = gson.getAdapter(TypeToken.get(valueType));

        // Build BiMap adapter
        return (TypeAdapter<T>) new BiMapAdapter<>(valueAdapter);
    }
}
