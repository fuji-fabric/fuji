package mod.fuji.core.config.mapper.adapter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import mod.fuji.core.config.mapper.GsonMapper;

public class CheckedEnumTypeValueAdapterFactory implements TypeAdapterFactory {

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        /* Ensure the type is class or subclasses of Enum<?>. */
        if (!Enum.class.isAssignableFrom(typeToken.getRawType())) {
            return null;
        }

        /* Ensure the type is a true enum, a concrete enum. */
        Class<T> typeClass = (Class<T>) typeToken.getType();
        if (!typeClass.isEnum()) {
            return null;
        }

        /* Capture the Gson's build-in enum type adapter, to respect @SerializedName annotation. */
        TypeAdapter<T> delegateAdapter = GsonMapper.getDelegateAdapter(this, typeToken);

        /* Make the enum type adapter. */
        return new CheckedEnumTypeValueAdapter<>(typeToken, delegateAdapter);
    }
}
