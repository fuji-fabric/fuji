package mod.fuji.module.initializer.document.config.adapter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import mod.fuji.core.document.auxiliary.DocumentUtil;
import mod.fuji.core.document.inspector.FailedToInspectException;
import mod.fuji.core.document.inspector.JavaObjectInspector;
import mod.fuji.module.initializer.document.config.writter.DocumentJsonWriter;

public class DocumentedTypeAdapterFactory implements TypeAdapterFactory {

    /**
     * The built document string map.
     * This map is model-specific. It should be cleared before the call to {@link Gson#toJson} method.
     * */
    public static final Map<String, String> FLATTEN_DOCUMENT_STRING_MAP = new HashMap<>();

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

        return new TypeAdapter<>() {

            @Override
            public void write(JsonWriter out, T value) throws IOException {
                /* Follow the Gson's recursive processing, and generate the document string map on the fly. */
                if (value != null) {
                    try {
                        JavaObjectInspector
                            .ofRoot(value)
                            .flatten()
                            // NOTE: Build the doc string map in down-top order, to prevent a child overrides a mappings from its parent.
                            .forEach(it -> DocumentUtil.putDocumentStringMap(FLATTEN_DOCUMENT_STRING_MAP, it.getObjectType()));
                    } catch (FailedToInspectException ignore) {
                        // Continue...
                    }
                }

                // NOTE: Wrap once to prevent resetting the JsonWriter internal states.
                if (!(out instanceof DocumentJsonWriter)) {
                    out = new DocumentJsonWriter(out);
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
