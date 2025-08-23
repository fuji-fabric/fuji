package io.github.sakurawald.fuji.core.config.migrator.transformer.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.sakurawald.fuji.core.config.migrator.transformer.abst.ConfigurationTransformer;
import io.github.sakurawald.fuji.core.structure.Pair;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RenameJsonKeysTransformer extends ConfigurationTransformer {
    final JsonObject jsonObject;
    final List<Pair<String, String>> renameRules;

    @Override
    protected boolean canApply() {
        return true;
    }

    @Override
    protected void apply() {

        renameRules.forEach(pair -> {
            String oldKey = pair.getKey();
            String newKey = pair.getValue();

            if (this.jsonObject.has(oldKey) && !this.jsonObject.has(newKey)) {
                JsonElement value = this.jsonObject.get(oldKey);
                this.jsonObject.add(newKey, value);
                this.jsonObject.remove(oldKey);
            }

        });

    }
}
