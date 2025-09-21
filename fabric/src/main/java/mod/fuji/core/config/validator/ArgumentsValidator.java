package mod.fuji.core.config.validator;

import com.google.gson.JsonObject;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.config.Configs;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.validator.structure.StringOccurenceMap;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public class ArgumentsValidator {

    public static void validateArguments(@NotNull BaseConfigurationHandler<?> handler, @NotNull JsonObject dataTree, @NotNull JsonObject schemaTree) {
        if (!Configs.MAIN_CONTROL_CONFIG.model().core.language.validator.validate_arguments) {
            return;
        }

        schemaTree
            .keySet()
            .forEach(key -> {
                validateArgumentCount("Java String Formatter", handler, StringOccurenceMap.JavaFormatterLanguage::makeOccurenceMap, key, dataTree, schemaTree);
                validateArgumentCount("Named Arguments", handler, StringOccurenceMap.NamedArgumentsLanguage::makeOccurenceMap, key, dataTree, schemaTree);
            });
    }

    private static void validateArgumentCount(@NotNull String checkName, @NotNull BaseConfigurationHandler<?> baseConfigurationHandler, @NotNull Function<String, StringOccurenceMap> mapper, @NotNull String languageKey, @NotNull JsonObject dataTree, @NotNull JsonObject schemaTree) {
        String schemaValue = schemaTree.get(languageKey).getAsString();
        String dataValue = dataTree.get(languageKey).getAsString();
        StringOccurenceMap schemaMap = mapper.apply(schemaValue);
        StringOccurenceMap dataMap = mapper.apply(dataValue);

        if (!schemaMap.equals(dataMap)) {
            LogUtil.warn("""

                [Arguments Validation Failed]
                The number of arguments for [{}] does not match between [actual language value] and [expected language value].
                Override the [actual language value] to match it now.

                ◉ Language File: {}
                ◉ Language Key: {}
                ◉ Actual Language Value: {}
                ◉ Expected Language Value: {}
                """, checkName, baseConfigurationHandler.getFilePath(), languageKey, dataValue, schemaValue);
            dataTree.addProperty(languageKey, schemaValue);
        }
    }
}
