package mod.fuji.module.initializer.command_spy.config.transformer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mod.fuji.core.auxiliary.JsonUtil;
import mod.fuji.core.config.migrator.transformer.abst.JsonConfigurationTransformer;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

public class CommandSpySchemaV1Transformer extends JsonConfigurationTransformer {

    @Override
    public String sinceVersion() {
        return "12.40.0";
    }

    @Override
    protected void apply() {
        this.readTargetJsonFile()
            .ifPresent(rootJsonObject -> {
                /* Read the value of flags. */
                AtomicBoolean overrideFile = new AtomicBoolean(false);
                boolean spyOnConsoleFlag = Optional
                    .ofNullable(rootJsonObject.get("spy_on_console"))
                    .map(JsonElement::getAsBoolean)
                    .orElse(false);

                /* Migrate 'ignore_commands' section. */
                JsonUtil.ifJsonElementPresent(rootJsonObject, "ignore_commands", JsonArray.class, ignoreCommandsJsonArray -> {
                    withRules(rootJsonObject, rulesJsonArray -> {
                        for (JsonElement ignoreCommandElement : ignoreCommandsJsonArray) {
                            JsonObject ruleJsonObject = makeCommandRule(ignoreCommandElement.getAsString(), spyOnConsoleFlag, false);
                            rulesJsonArray.add(ruleJsonObject);
                            overrideFile.set(true);
                        }
                    });
                });

                /* Migrate 'only_spy_these_commands' section. */
                JsonUtil.ifJsonElementPresent(rootJsonObject, "only_spy_these_commands", JsonObject.class, onlySpyTheseCommands -> {
                    withRules(rootJsonObject, rulesJsonArray -> {
                        boolean enable = onlySpyTheseCommands.get("enable").getAsBoolean();

                        if (enable) {
                            JsonArray commands = onlySpyTheseCommands.get("commands").getAsJsonArray();
                            for (JsonElement command : commands) {
                                JsonObject rule = makeCommandRule(command.getAsString(), spyOnConsoleFlag, true);
                                rulesJsonArray.add(rule);
                                overrideFile.set(true);
                            }
                        } else {
                            JsonObject rule = makeCommandRule(".+", spyOnConsoleFlag, true);
                            rulesJsonArray.add(rule);
                            overrideFile.set(true);
                        }

                    });
                });

                /* Override file. */
                if (overrideFile.get()) {
                    writeTargetJsonFile(rootJsonObject);
                }
            });
    }

    private static JsonObject makeCommandRule(@NotNull String commandString, boolean spyOnConsoleFlag, boolean logToConsoleFlag) {
        JsonObject rule = new JsonObject();
        rule.addProperty("enable", true);

        JsonObject matcher = new JsonObject();
        JsonObject ifMatched = new JsonObject();
        rule.add("matcher", matcher);
        rule.add("if_matched", ifMatched);

        matcher.addProperty("command_string_regex", commandString);
        matcher.addProperty("accept_silent_command", true);

        matcher.addProperty("accept_player_command_source", true);
        matcher.addProperty("accept_server_command_source", spyOnConsoleFlag);

        ifMatched.addProperty("log_to_console", logToConsoleFlag);
        ifMatched.addProperty("notify_players_with_level_permission", 5);

        return rule;
    }

    private static void withRules(@NotNull JsonObject rootJsonObject, Consumer<JsonArray> consumer) {
        final String rulesJsonKey = "rules";

        JsonArray value = Optional
            .ofNullable(rootJsonObject.get(rulesJsonKey))
            .map(JsonElement::getAsJsonArray)
            .orElseGet(() -> {
                JsonArray newValue = new JsonArray();
                rootJsonObject.add(rulesJsonKey, newValue);
                return newValue;
            });

        consumer.accept(value);
    }
}
