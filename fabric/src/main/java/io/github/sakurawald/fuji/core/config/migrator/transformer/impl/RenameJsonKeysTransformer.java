package io.github.sakurawald.fuji.core.config.migrator.transformer.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.sakurawald.fuji.core.config.migrator.transformer.abst.ConfigurationTransformer;
import io.github.sakurawald.fuji.core.structure.Pair;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class RenameJsonKeysTransformer extends ConfigurationTransformer {
    final @NotNull JsonObject jsonObject;
    final @NotNull List<Pair<String, String>> renameRules;

    @Override
    protected boolean canApply() {
        return renameRules
                .stream()
                .anyMatch(rule -> {
            String oldJsonKey = rule.getKey();
            return this.jsonObject.has(oldJsonKey);
        });
    }

    @Override
    protected void apply() {
        renameRules.forEach(rule -> {
            String oldJsonKey = rule.getKey();
            String newJsonKey = rule.getValue();

            if (this.jsonObject.has(oldJsonKey) && !this.jsonObject.has(newJsonKey)) {
                JsonElement value = this.jsonObject.get(oldJsonKey);
                this.jsonObject.add(newJsonKey, value);
                this.jsonObject.remove(oldJsonKey);
            }

        });

    }
}
