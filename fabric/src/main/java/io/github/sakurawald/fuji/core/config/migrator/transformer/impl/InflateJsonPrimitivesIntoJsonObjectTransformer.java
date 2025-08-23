package io.github.sakurawald.fuji.core.config.migrator.transformer.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.sakurawald.fuji.core.config.migrator.transformer.abst.ConfigurationTransformer;
import java.util.List;
import java.util.function.Function;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class InflateJsonPrimitivesIntoJsonObjectTransformer extends ConfigurationTransformer {

    final @NotNull JsonObject sourceJsonObject;
    final @NotNull List<String> jsonKeys;
    final @NotNull Function<JsonObject, JsonObject> destinationJsonObjectMaker;

    @Override
    protected boolean canApply() {
        return jsonKeys
            .stream()
            .allMatch(this.sourceJsonObject::has);
    }

    @Override
    protected void apply() {
        @NotNull JsonObject sourceJsonObject = this.sourceJsonObject;
        @NotNull List<String> jsonKeys = this.jsonKeys;
        @NotNull JsonObject destinationJsonObject = this.destinationJsonObjectMaker.apply(sourceJsonObject);

        jsonKeys.forEach(jsonKey -> {
            if (!destinationJsonObject.has(jsonKey)) {
                JsonElement jsonValue = this.sourceJsonObject.get(jsonKey);
                destinationJsonObject.add(jsonKey, jsonValue);
            }
        });

        jsonKeys.forEach(sourceJsonObject::remove);
    }

}
