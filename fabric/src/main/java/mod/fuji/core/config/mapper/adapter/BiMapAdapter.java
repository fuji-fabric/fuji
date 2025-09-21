package mod.fuji.core.config.mapper.adapter;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Map;

public class BiMapAdapter<V> extends TypeAdapter<BiMap<String, V>> {
    private final TypeAdapter<V> valueTypeAdapter;

    public BiMapAdapter(TypeAdapter<V> valueTypeAdapter) {
        this.valueTypeAdapter = valueTypeAdapter;
    }

    @Override
    public void write(JsonWriter out, BiMap<String, V> biMap) throws IOException {
        out.beginObject();
        for (Map.Entry<String, V> entry : biMap.entrySet()) {
            // Keys must be strings in JSON objects.
            out.name(String.valueOf(entry.getKey()));
            valueTypeAdapter.write(out, entry.getValue());
        }
        out.endObject();
    }

    @Override
    public BiMap<String, V> read(JsonReader in) throws IOException {
        BiMap<String, V> biMap = HashBiMap.create();
        in.beginObject();
        while (in.hasNext()) {
            String key = in.nextName();
            V value = valueTypeAdapter.read(in);
            biMap.put(key, value);
        }
        in.endObject();
        return biMap;
    }
}
