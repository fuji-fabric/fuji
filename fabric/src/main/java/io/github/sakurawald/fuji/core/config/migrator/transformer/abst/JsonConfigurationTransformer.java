package io.github.sakurawald.fuji.core.config.migrator.transformer.abst;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.PathNotFoundException;
import io.github.sakurawald.fuji.core.auxiliary.JsonUtil;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.config.migrator.version.SemVerComparator;
import io.github.sakurawald.fuji.core.config.migrator.version.VersionPropertyInjector;
import io.github.sakurawald.fuji.core.config.parser.JsonPathParser;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public abstract class JsonConfigurationTransformer extends ConfigurationTransformer {

    @Getter(lazy = true)
    private final DocumentContext jsonDocumentContext = makeJsonDocumentContext();

    @SneakyThrows(IOException.class)
    private DocumentContext makeJsonDocumentContext() {
        return JsonPathParser.getJsonPathParser().parse(this.targetFilePath.toFile());
    }

    public @NotNull JsonObject readRootJsonObject() {
        return getJsonDocumentContext().read("$");
    }

    public boolean existsJsonPath(@NotNull DocumentContext context, @NotNull String jsonPath) {
        try {
            context.read(jsonPath);
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    public boolean notExistsJsonPath(@NotNull DocumentContext context, @NotNull String jsonPath) {
        return !existsJsonPath(context, jsonPath);
    }

    public Object getJsonPath(@NotNull DocumentContext context, @NotNull String jsonPath) {
        return context.read(jsonPath);
    }

    public void setJsonPath(@NotNull DocumentContext context, @NotNull String jsonPath, @NotNull Object newValue) {
        this.logOperation("Set json path: path = {}, value = {}", jsonPath, newValue);
        context.set(jsonPath, newValue);
    }

    public void writeJsonDocumentContextToOriginalFile(@NotNull DocumentContext context) {
        this.logOperation("Write storage.");
        JsonUtil.writeJsonObject(context.json(), this.targetFilePath);
    }

    public void writeJsonObjectToOriginalFile(@NotNull JsonObject jsonObject) {
        this.logOperation("Write storage.");
        JsonUtil.writeJsonObject(jsonObject, this.targetFilePath);
    }

    public abstract String sinceVersion();

    @Override
    protected boolean canApply() {
        DocumentContext documentContext = getJsonDocumentContext();
        String jsonVersionString = getJsonVersion(documentContext);
        String sinceVersionString = sinceVersion();
        boolean canApply = SemVerComparator.compareSemVer(jsonVersionString, sinceVersionString) <= 0;

        LogUtil.debug("Check if the transformer can be applied: file path = {}, json version string = {}, since version string = {}, can apply = {}", this.targetFilePath, jsonVersionString, sinceVersionString, canApply);
        return canApply;
    }

    private @NotNull String getJsonVersion(@NotNull DocumentContext documentContext) {
        String jsonVersionString;
        try {
            jsonVersionString = ((JsonPrimitive) getJsonPath(documentContext, "$.MOD_VERSION")).getAsString();
        } catch (PathNotFoundException e) {
            LogUtil.warn("There is no mod version string in file {}, treating it as {}", this.targetFilePath, VersionPropertyInjector.UNKNOWN_MOD_VERSION);
            jsonVersionString = VersionPropertyInjector.UNKNOWN_MOD_VERSION;
        }

        return jsonVersionString;
    }
}
