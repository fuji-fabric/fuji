package tests.mixin;

import auxiliary.ClassGraphUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.auxiliary.JsonUtil;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.Mixin;

public class CheckMixinRegistryTest {

    private List<JsonElement> collectJsonArray(JsonElement jsonElement, String jsonKey) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        // NOTE: Allow to not write the `client` key.
        if (!jsonObject.has(jsonKey)) return Collections.emptyList();

        JsonArray mixinsArray = jsonObject.get(jsonKey).getAsJsonArray();
        return mixinsArray.asList();
    }

    private List<String> collectMixins(JsonElement jsonElement, String jsonKey) {
        List<JsonElement> jsonElements = collectJsonArray(jsonElement, jsonKey);
        return jsonElements
            .stream()
            .map(JsonElement::getAsString)
            .toList();
    }

    @SneakyThrows
    @Test
    public void ensureAllMixinClassIsRegisteredInMixinJsonFile() {
        /* Read the fuji.mixins.json file, to get the registered mixins. */
        Path mixinJsonFilePath = Path.of("src/main/resources/fuji.mixins.json");
        JsonElement mixinJsonFileJson = JsonUtil.readJsonFile(mixinJsonFilePath);
        List<String> registeredMixins = new ArrayList<>();
        registeredMixins.addAll(collectMixins(mixinJsonFileJson, "mixins"));
        registeredMixins.addAll(collectMixins(mixinJsonFileJson, "client"));
        registeredMixins.addAll(collectMixins(mixinJsonFileJson, "server"));

        /* Scan the codebase, to find un-registered mixins. */
        String mixinPackage = Fuji.class.getPackageName() + ".module.mixin";
        List<String> unregisteredMixins = new ArrayList<>();
        try (ScanResult scanResult = ClassGraphUtil
            .makeBaseClassGraph()
            .enableAllInfo()
            .scan()) {

            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(Mixin.class)) {
                String mixinSimpleName = classInfo.getName().substring(mixinPackage.length() + 1);
                if (!registeredMixins.contains(mixinSimpleName)) {
                    unregisteredMixins.add(mixinSimpleName);
                }
            }
        }

        /* Check result. */
        if (!unregisteredMixins.isEmpty()) {
            throw new RuntimeException("The following mixins are not registered: " + unregisteredMixins);
        }
    }

}

