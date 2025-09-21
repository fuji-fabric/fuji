package mod.fuji.core.config.validator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mod.fuji.core.auxiliary.LogUtil;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class RemoveNullElementsInJsonArrayValidator {

    public static void validate(@NotNull JsonObject dataTree) {
        dataTree
            .keySet()
            .forEach(key -> {
                JsonElement jsonElement = dataTree.get(key);

                /* Go down. */
                if (jsonElement.isJsonObject()) {
                    validate(jsonElement.getAsJsonObject());
                    return;
                }

                /* Go up. */
                if (!jsonElement.isJsonArray()) {
                    return;
                }

                JsonArray jsonArray = dataTree.get(key).getAsJsonArray();
                List<JsonElement> arrayElementsToRemove = new ArrayList<>();
                jsonArray.forEach(arrayElement -> {
                    if (arrayElement.isJsonObject()) {
                        validate(arrayElement.getAsJsonObject());
                    }

                    if (arrayElement.isJsonNull()) {
                        arrayElementsToRemove.add(arrayElement);
                    }
                });

                if (!arrayElementsToRemove.isEmpty()) {
                    LogUtil.warn("Remove JsonNull elements {} in JsonArray {}", arrayElementsToRemove, jsonArray);
                    arrayElementsToRemove.forEach(jsonArray::remove);
                }
            });

    }
}
