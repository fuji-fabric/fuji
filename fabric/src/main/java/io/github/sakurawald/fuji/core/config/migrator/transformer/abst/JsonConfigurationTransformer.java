package io.github.sakurawald.fuji.core.config.migrator.transformer.abst;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.sakurawald.fuji.core.auxiliary.JsonUtil;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.config.migrator.version.SemVerComparator;
import io.github.sakurawald.fuji.core.config.migrator.version.VersionPropertyInjector;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public abstract class JsonConfigurationTransformer extends ConfigurationTransformer {

    public abstract String sinceVersion();

    @Override
    protected boolean canApply() {
        String jsonVersionString = getJsonVersion();
        String sinceVersionString = sinceVersion();
        return SemVerComparator.compareSemVer(jsonVersionString, sinceVersionString) <= 0;
    }

    private @NotNull String getJsonVersion() {
        JsonObject jsonObject = readTargetJsonFile();

        return Optional
            .ofNullable(jsonObject.get(VersionPropertyInjector.MOD_VERSION_KEY))
            .map(JsonElement::getAsString)
            .orElseGet(() -> {
                LogUtil.warn("There is no mod version string in file {}, treating it as {}", this.targetFilePath, VersionPropertyInjector.UNKNOWN_MOD_VERSION);
                return VersionPropertyInjector.UNKNOWN_MOD_VERSION;
            });
    }

    protected @NotNull JsonObject readTargetJsonFile() {
        return JsonUtil.readJsonFile(this.getTargetFilePath());
    }

    protected void writeTargetJsonFile(@NotNull JsonObject rootJsonObject) {
        logOperation("Override the original file.");
        JsonUtil.writeJsonObject(rootJsonObject, this.targetFilePath);
    }
}
