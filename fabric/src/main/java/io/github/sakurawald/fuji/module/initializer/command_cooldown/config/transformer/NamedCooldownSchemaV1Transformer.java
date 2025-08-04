package io.github.sakurawald.fuji.module.initializer.command_cooldown.config.transformer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.DocumentContext;
import io.github.sakurawald.fuji.core.auxiliary.JsonUtil;
import io.github.sakurawald.fuji.core.config.transformer.abst.JsonConfigurationTransformer;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.CommandCooldownInitializer;

public class NamedCooldownSchemaV1Transformer extends JsonConfigurationTransformer {

    @SuppressWarnings({"UnnecessaryLocalVariable", "SizeReplaceableByIsEmpty"})
    @Override
    public void apply() {
        /* Check requisition. */
        DocumentContext context = getJsonDocumentContext();
        JsonArray jsonArray = context.read("$.named_cooldown.list.*.timestamp");
        if (jsonArray.size() == 0) {
            return;
        }

        /* Make the output json object. */
        JsonObject input$list = context.read("$.named_cooldown.list");
        JsonObject outputRoot = new JsonObject();
        JsonArray output$nodes = new JsonArray();
        outputRoot.add("nodes", output$nodes);

        input$list
            .keySet()
            .forEach(key -> {
                String namedCooldownId = key;
                JsonObject namedCooldownDescriptor = input$list.get(namedCooldownId).getAsJsonObject();
                JsonObject outputObject = new JsonObject();

                /* Migrate the id field. */
                outputObject.addProperty("id", namedCooldownId);

                /* Migrate the timestamp field. */
                JsonElement cooldownObject = namedCooldownDescriptor.get("timestamp");
                JsonObject cooldownField = new JsonObject();
                cooldownField.add("timestamp", cooldownObject);
                outputObject.add("cooldown", cooldownField);

                /* Migrate the uses field. */
                JsonElement usesField = namedCooldownDescriptor.get("uses");
                outputObject.add("uses", usesField);

                output$nodes.add(outputObject);
            });

        /* Write it. */
        JsonUtil.writeJsonObject(outputRoot, CommandCooldownInitializer.namedCooldownData.getPath());
    }
}
