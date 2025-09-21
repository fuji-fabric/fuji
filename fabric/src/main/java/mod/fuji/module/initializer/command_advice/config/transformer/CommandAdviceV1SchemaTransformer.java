package mod.fuji.module.initializer.command_advice.config.transformer;

import com.google.gson.JsonObject;
import mod.fuji.core.config.migrator.transformer.abst.JsonConfigurationTransformer;
import mod.fuji.core.config.migrator.transformer.impl.InflateJsonPrimitivesIntoJsonObjectTransformer;
import mod.fuji.core.config.migrator.transformer.impl.RenameJsonKeysTransformer;
import mod.fuji.core.structure.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommandAdviceV1SchemaTransformer extends JsonConfigurationTransformer {

    @Override
    public String sinceVersion() {
        return "12.27.0";
    }

    @Override
    protected void apply() {
        readTargetJsonFile().ifPresent(rootJsonObject -> {
                Optional
                    .ofNullable(rootJsonObject.get("entries"))
                    .ifPresent(it -> {
                        /* Iterate the existing entries. */
                        it.getAsJsonArray().forEach(arrayElement -> {
                            List<JsonObject> destinationJsonObjects = new ArrayList<>();

                            new InflateJsonPrimitivesIntoJsonObjectTransformer(arrayElement.getAsJsonObject(), List.of("match_command_string_regex", "only_valid_when_command_is_executed_by_player"), $sourceJsonObject -> {
                                JsonObject destinationJsonObject = new JsonObject();
                                $sourceJsonObject.add("matcher", destinationJsonObject);
                                destinationJsonObjects.add(destinationJsonObject);
                                return destinationJsonObject;
                            })
                                .tryApply(this.getTargetFilePath());

                            destinationJsonObjects.forEach(jsonObject -> new RenameJsonKeysTransformer(jsonObject, List.of(
                                new Pair<>("match_command_string_regex", "command_string_regex"),
                                new Pair<>("only_valid_when_command_is_executed_by_player", "executed_by_player_only")
                            )).tryApply(this.getTargetFilePath()));

                        });

                        /* Write target file. */
                        writeTargetJsonFile(rootJsonObject);
                    });
            });

    }

}
