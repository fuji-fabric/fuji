package io.github.sakurawald.fuji.module.initializer.skin.config.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.authlib.properties.Property;
import java.io.IOException;

public class PropertyAdapter extends TypeAdapter<Property> {

    @Override
    public void write(JsonWriter out, Property p) throws IOException {
        out.beginObject();
        out.name("name").value(p.getName());
        out.name("value").value(p.getValue());
        if (p.getSignature() != null) {
            out.name("signature").value(p.getSignature());
        }
        out.endObject();
    }

    @Override
    public Property read(JsonReader in) throws IOException {
        String name = null, value = null, signature = null;

        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "name":      name = in.nextString(); break;
                case "value":     value = in.nextString(); break;
                case "signature": signature = in.nextString(); break;
                default:          in.skipValue();
            }
        }
        in.endObject();

        return new Property(name, value, signature);
    }
}
