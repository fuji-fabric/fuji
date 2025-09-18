package io.github.sakurawald.fuji.module.initializer.document.config.adapter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.sakurawald.fuji.module.initializer.document.config.writter.DocumentJsonWriter;
import io.github.sakurawald.fuji.core.document.auxiliary.DocumentUtil;
import java.io.IOException;
import java.util.Map;

public class DocumentedTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
        Map<String, String> declaredDocumentStrings = DocumentUtil.getDeclaredDocumentStringMap(type.getRawType());

        return new TypeAdapter<>() {

            @Override
            public void write(JsonWriter out, T value) throws IOException {
                // NOTE: Wrap once to prevent resetting the JsonWriter internal states.
                if (!(out instanceof DocumentJsonWriter)) {
                    out = new DocumentJsonWriter(out, declaredDocumentStrings);
                }

                delegate.write(out, value);
            }

            @Override
            public T read(JsonReader in) throws IOException {
                return delegate.read(in);
            }
        };
    }

}
