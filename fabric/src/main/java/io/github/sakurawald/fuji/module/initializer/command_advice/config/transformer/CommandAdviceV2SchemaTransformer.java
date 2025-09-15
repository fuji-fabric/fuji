package io.github.sakurawald.fuji.module.initializer.command_advice.config.transformer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.sakurawald.fuji.core.auxiliary.JsonUtil;
import io.github.sakurawald.fuji.core.config.migrator.transformer.abst.JsonConfigurationTransformer;

public class CommandAdviceV2SchemaTransformer extends JsonConfigurationTransformer {

    @Override
    public String sinceVersion() {
        return "12.47.0";
    }

    @Override
    protected void apply() {
        withTargetJsonFile((rootJsonObject, overrideFlag) -> {
            JsonUtil.ifJsonElementPresent(rootJsonObject, "advices", JsonArray.class, advices ->
                advices.forEach(advice -> {
                    JsonUtil.ifJsonElementPresent(advice.getAsJsonObject(), "matcher", JsonObject.class, matcher -> {
                        JsonUtil.ifJsonElementPresent(matcher, "executed_by_player_only", JsonPrimitive.class, executedByPlayerOnly -> {
                            boolean $executedByPlayerOnly = executedByPlayerOnly.getAsBoolean();
                            migrateExecutedByPlayerOnlyField(matcher, $executedByPlayerOnly);
                            overrideFlag.set(true);
                        });
                    });
                }));
        });
    }

    private static void migrateExecutedByPlayerOnlyField(JsonObject matcher, boolean executedByPlayerOnly) {
        if (executedByPlayerOnly) {
            matcher.addProperty("accept_player_command_source", true);
            matcher.addProperty("accept_console_command_source", false);
        } else {
            matcher.addProperty("accept_player_command_source", true);
            matcher.addProperty("accept_console_command_source", true);
        }
    }

}
