package io.github.sakurawald.fuji.module.initializer.command_warmup.config.transformer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.sakurawald.fuji.core.auxiliary.JsonUtil;
import io.github.sakurawald.fuji.core.config.migrator.transformer.abst.JsonConfigurationTransformer;
import java.util.concurrent.atomic.AtomicBoolean;

public class CommandWarmupV1SchemaTransformer extends JsonConfigurationTransformer {
    @Override
    public String sinceVersion() {
        return "12.28.0";
    }

    @Override
    protected void apply() {
        readTargetJsonFile().ifPresent(rootJsonObject -> {
            AtomicBoolean overrideFlag = new AtomicBoolean(false);

            JsonUtil.ifJsonElementPresent(rootJsonObject, "rules", JsonArray.class, rulesArray -> {
                rulesArray.forEach(ruleArrayElement -> {
                    if (ruleArrayElement.isJsonObject()) {
                        JsonObject rule = ruleArrayElement.getAsJsonObject();
                        JsonUtil.ifJsonElementPresent(rule, "tag", JsonObject.class, tagJsonObject -> {
                            JsonUtil.ifJsonElementPresent(tagJsonObject, "tags", JsonArray.class, tagsArray -> {
                                rule.add("tags", tagsArray);
                                overrideFlag.set(true);
                            });
                        });
                    }
                });
            });

            if (overrideFlag.get()) {
                writeTargetJsonFile(rootJsonObject);
            }
        });

    }
}
