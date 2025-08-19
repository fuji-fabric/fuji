package io.github.sakurawald.fuji.core.config.handler.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.config.Configs;
import io.github.sakurawald.fuji.core.config.exception.FailedToLoadResourceException;
import io.github.sakurawald.fuji.core.config.structure.StringOccurenceMap;
import io.github.sakurawald.fuji.core.config.transformer.impl.MoveFileTransformer;
import io.github.sakurawald.fuji.core.document.structure.DocString;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public class LanguageConfigurationHandler extends ResourceConfigurationHandler {

    private static final String LANGUAGE_DIRECTORY_NAME = "languages";
    private static final String LANGUAGE_FILE_CLASS_PATH_PREFIX = "/" + LANGUAGE_DIRECTORY_NAME + "/";
    private static final String FALLBACK_LANGUAGE_FILE_CLASS_PATH = LANGUAGE_FILE_CLASS_PATH_PREFIX + toLanguageFileName("en_US");

    public LanguageConfigurationHandler(@NotNull String languageCode) {
        super(getLanguageFilePath(languageCode), getLanguageFileClassPath(languageCode));
        this.installTransformer(new MoveFileTransformer(Fuji.MOD_CONFIG_PATH.resolve("lang"), Fuji.MOD_CONFIG_PATH.resolve("languages")));
    }

    private static @NotNull Path getLanguageFilePath(@NotNull String languageCode) {
        String languageFileName = toLanguageFileName(languageCode);
        return Fuji.MOD_CONFIG_PATH
            .resolve(LANGUAGE_DIRECTORY_NAME)
            .resolve(languageFileName);
    }

    private static @NotNull String getLanguageFileClassPath(@NotNull String languageCode) {
        return LANGUAGE_FILE_CLASS_PATH_PREFIX + toLanguageFileName(languageCode);
    }

    private static @NotNull String toLanguageFileName(@NotNull String languageCode) {
        return languageCode + ".json";
    }

    public static @NotNull String toLanguageCode(@NotNull String languageFileName) {
        return languageFileName.replace(".json", "");
    }

    @Override
    protected void beforeWriteStorage() {
        this.model = makeSortedLanguageJsonObject((JsonObject) this.model);
    }

    public static @NotNull JsonObject makeSortedLanguageJsonObject(@NotNull JsonObject original) {
        Map<String, JsonElement> sortedMap = new TreeMap<>((a, b) -> {
            boolean aIsDocString = a.startsWith(DocString.DOC_STRING_KEY_PREFIX);
            boolean bIsDocString = b.startsWith(DocString.DOC_STRING_KEY_PREFIX);

            if (aIsDocString && !bIsDocString) return +1;
            if (!aIsDocString && bIsDocString) return -1;

            //noinspection ConstantValue
            if (aIsDocString && bIsDocString) {
                long aId = DocString.parseDocStringId(a);
                long bId = DocString.parseDocStringId(b);
                return Long.compare(aId, bId);
            }

            return a.compareTo(b);
        });

        for (Map.Entry<String, JsonElement> entry : original.entrySet()) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        JsonObject sortedJson = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : sortedMap.entrySet()) {
            sortedJson.add(entry.getKey(), entry.getValue());
        }

        return sortedJson;
    }

    @Override
    protected JsonElement getDefaultModel() {
        // NOTE: When `language` module is enabled, a player joined with an un-supported language `aa_BB` the first time, a file `lang/aa_BB.json` will be created.
        try {
            return readJsonTreeFromResource(this.resourceClassPath);
        } catch (FailedToLoadResourceException e) {
            LogUtil.debug("Failed to make the default configuration model from `{}` resource path. (Fallback to the `{}`)", this.resourceClassPath, FALLBACK_LANGUAGE_FILE_CLASS_PATH);
            return readJsonTreeFromResource(FALLBACK_LANGUAGE_FILE_CLASS_PATH);
        }
    }

    @Override
    protected void validateModel(@NotNull JsonObject dataTree, @NotNull JsonObject schemaTree) {
        super.validateModel(dataTree, schemaTree);
        validateArguments(dataTree, schemaTree);
    }

    private void validateArguments(@NotNull JsonObject dataTree, @NotNull JsonObject schemaTree) {
        if (!Configs.MAIN_CONTROL_CONFIG.model().core.language.validator.validate_arguments) {
            return;
        }

        schemaTree
            .keySet()
            .forEach(key -> {
                validateArgumentCount("Java String Formatter", StringOccurenceMap.JavaFormatterLanguage::makeOccurenceMap, key, dataTree, schemaTree);
                validateArgumentCount("Named Arguments", StringOccurenceMap.NamedArgumentsLanguage::makeOccurenceMap, key, dataTree, schemaTree);
            });
    }

    private void validateArgumentCount(@NotNull String checkName, @NotNull Function<String, StringOccurenceMap> mapper, @NotNull String languageKey, @NotNull JsonObject dataTree, @NotNull JsonObject schemaTree) {
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
                """, checkName, this.getPath(), languageKey, dataValue, schemaValue);
            dataTree.addProperty(languageKey, schemaValue);
        }
    }
}

