package mod.fuji.module.initializer.system_message.config.transformer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mod.fuji.core.auxiliary.JsonUtil;
import mod.fuji.core.config.migrator.transformer.abst.JsonConfigurationTransformer;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SystemMessageV1SchemaTransformer extends JsonConfigurationTransformer {

    @Override
    public String sinceVersion() {
        return "12.48.0";
    }

    @Override
    protected void apply() {
        withTargetJsonFile((rootJsonObject, override) -> {
            JsonUtil.ifJsonElementPresent(rootJsonObject,"rules", JsonObject.class, rules -> {
                JsonArray rulesArray = new JsonArray();

                rules.keySet().forEach(translatableKey -> {
                    @Nullable String translatableValue = Optional
                        .ofNullable(rules.get(translatableKey))
                        .filter(it -> !it.isJsonNull())
                        .map(JsonElement::getAsString)
                        .orElse(null);
                    JsonObject ruleJsonObject = makeRuleJsonObject(translatableKey, translatableValue);
                    rulesArray.add(ruleJsonObject);
                });

                rootJsonObject.add("rules", rulesArray);
                override.set(true);
            });
        });
    }

    private static @NotNull JsonObject makeRuleJsonObject(@NotNull String translatableKey, @Nullable String translatableValue) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("enable", true);
        jsonObject.add("document", null);
        jsonObject.addProperty("translatable_text_key", translatableKey);
        jsonObject.addProperty("translatable_text_value", translatableValue);
        return jsonObject;
    }


}
