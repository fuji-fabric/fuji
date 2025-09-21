package mod.fuji.core.config.migrator.transformer.abst;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mod.fuji.core.auxiliary.JsonUtil;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.config.migrator.version.SemVerComparator;
import mod.fuji.core.config.migrator.version.VersionPropertyInjector;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;

public abstract class JsonConfigurationTransformer extends ConfigurationTransformer {

    public abstract String sinceVersion();

    @Override
    protected boolean canApply() {
        String jsonVersionString = getJsonVersion();
        String sinceVersionString = sinceVersion();
        return SemVerComparator.compareSemVer(jsonVersionString, sinceVersionString) < 0;
    }

    private @NotNull String getJsonVersion() {
        try {
            /* If the target file not exists, treating it as the legacy mod version, and apply all installed transformers. */
            if (Files.notExists(this.getTargetFilePath())) {
                return VersionPropertyInjector.LEGACY_MOD_VERSION;
            }

            /* Read the mod version from the existing target file. */
            return readTargetJsonFile()
                .map(jsonObject -> jsonObject.get(VersionPropertyInjector.MOD_VERSION_KEY))
                .map(JsonElement::getAsString)
                .orElseGet(() -> {
                    String fallbackModVersion = VersionPropertyInjector.LEGACY_MOD_VERSION;
                    LogUtil.warn("There is no mod version string in file {}, treating it as {}", this.targetFilePath, fallbackModVersion);
                    return fallbackModVersion;
                });
        } catch (Exception e) {
            String fallbackModVersion = VersionPropertyInjector.LEGACY_MOD_VERSION;
            LogUtil.warn("Failed to read the file to get the mod version string, treating it as {}", this.targetFilePath, fallbackModVersion);
            return fallbackModVersion;
        }
    }

    protected @NotNull Optional<JsonObject> readTargetJsonFile() {
        try {
            return Optional.of(JsonUtil.readJsonFile(this.getTargetFilePath()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    protected void writeTargetJsonFile(@NotNull JsonObject rootJsonObject) {
        logOperation("Transformer {} applied, now override the original file.", this.getClass().getName());
        JsonUtil.writeJsonObject(rootJsonObject, this.targetFilePath);
    }

    protected void withTargetJsonFile(BiConsumer<JsonObject, AtomicBoolean> consumer) {
        readTargetJsonFile()
            .ifPresent(jsonObject -> {
                AtomicBoolean overrideTargetFile = new AtomicBoolean(false);

                consumer.accept(jsonObject, overrideTargetFile);

                if (overrideTargetFile.get()) {
                    writeTargetJsonFile(jsonObject);
                }
            });

    }
}
