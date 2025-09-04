package tests.mixin;

import auxiliary.ClassGraphUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.auxiliary.JsonUtil;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.Mixin;

public class GenerateMixinRegistryTest {

    @Test
    public void ensureAllMixinClassIsRegisteredInMixinJsonFile() {

        /* Read the fuji.mixins.json file, to get the registered mixins. */
        Path mixinJsonFilePath = Path.of("src/main/resources/fuji.mixins.json");
        JsonElement mixinJsonFileJson = JsonUtil.readJsonFile(mixinJsonFilePath);

        /* Scan the codebase, to collect declared mixin classes. */
        String mixinPackageName = Fuji.class.getPackageName() + ".module.mixin";
        List<String> collectedMixinNames = new ArrayList<>();
        try (ScanResult scanResult = ClassGraphUtil
            .makeBaseClassGraph()
            .enableAllInfo()
            .scan()) {

            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(Mixin.class)) {
                String mixinSimpleName = classInfo.getName().substring(mixinPackageName.length() + 1);
                collectedMixinNames.add(mixinSimpleName);
            }
        }

        /* Write the mixin graph file. */
        JsonArray mixinJsonArray = new JsonArray();
        collectedMixinNames.forEach(mixinJsonArray::add);
        mixinJsonFileJson
            .getAsJsonObject()
            .add("mixins", mixinJsonArray);

        JsonUtil.writeJsonObject(mixinJsonFileJson.getAsJsonObject(), mixinJsonFilePath);
    }

}

