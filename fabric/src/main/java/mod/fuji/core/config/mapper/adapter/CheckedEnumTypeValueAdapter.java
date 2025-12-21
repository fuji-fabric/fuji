package mod.fuji.core.config.mapper.adapter;

import com.google.errorprone.annotations.CheckReturnValue;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.ReflectionUtil;
import mod.fuji.core.config.mapper.GsonMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class CheckedEnumTypeValueAdapter<T> extends TypeAdapter<T> {

    private final TypeToken<T> typeToken;
    private final TypeAdapter<T> delegate;

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        this.delegate.write(out, value);
    }

    @Override
    public T read(JsonReader in) throws IOException {
        Class<? super T> concreteEnumClass = typeToken.getRawType();

        /* Validate the non-null java type constraint. */
        if (!GsonMapper.TypeNullability.isNullableType(concreteEnumClass) && in.peek() == JsonToken.NULL) {
            throw handleInvalidEnumValueException(in, concreteEnumClass, null);
        }

        /* Validate the enum values. */
        if (in.peek() == JsonToken.NULL) {
            @Nullable T readInNullToken = delegate.read(in);
            return readInNullToken;
        } else {
            String nextString = in.nextString();
            JsonPrimitive nextStringJsonElement = new JsonPrimitive(nextString);
            @Nullable T read = delegate.fromJsonTree(nextStringJsonElement);

            // Ouch, a non-null JSON string resulted a null Java object, that must be an invalid string.
            if (read == null) {
                throw handleInvalidEnumValueException(in, concreteEnumClass, nextString);
            }
            return read;
        }
    }

    @CheckReturnValue
    private IllegalArgumentException handleInvalidEnumValueException(@NotNull JsonReader in, @NotNull Class<?> concreteEnumClass, @Nullable String inputJsnoString) {
        LogUtil.error("""


            [Enum value is invalid]
            The enum value of type '{}' is invalid.

            ◉ Json Path: {}
            ◉ Current enum value: {}
            ◉ Acceptable enum values: {}
            """, concreteEnumClass.getName(), in.getPath(), inputJsnoString, ReflectionUtil.getEnumValuesCompactString(concreteEnumClass));
        return new IllegalArgumentException("Invalid enum value detected.");
    }
}
