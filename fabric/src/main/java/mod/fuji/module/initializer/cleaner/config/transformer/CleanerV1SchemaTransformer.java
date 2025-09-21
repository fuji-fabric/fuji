package mod.fuji.module.initializer.cleaner.config.transformer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mod.fuji.core.auxiliary.JsonUtil;
import mod.fuji.core.config.migrator.transformer.abst.JsonConfigurationTransformer;
import java.util.concurrent.atomic.AtomicBoolean;

public class CleanerV1SchemaTransformer extends JsonConfigurationTransformer {

    @Override
    public String sinceVersion() {
        return "12.38.0";
    }

    @Override
    protected void apply() {
        this.readTargetJsonFile().ifPresent(rootJsonObject -> {
            AtomicBoolean overrideFlag = new AtomicBoolean(false);

            JsonUtil.ifJsonElementPresent(rootJsonObject, "key2age", JsonObject.class, key2ageJsonObject -> {
                overrideFlag.set(true);

                JsonArray matchers = new JsonArray();
                key2ageJsonObject.entrySet().forEach(entry -> {
                    JsonObject matcher = new JsonObject();
                    matcher.addProperty("enable", true);
                    matcher.addProperty("translatable_key", entry.getKey());
                    matcher.add("lives_longer_than_age", entry.getValue());
                    matcher.addProperty("cleanup_method", "DISCARD");
                    matchers.add(matcher);
                });

                rootJsonObject.add("matchers", matchers);
            });

            if (overrideFlag.get()) {
                writeTargetJsonFile(rootJsonObject);
            }
        });

    }
}
