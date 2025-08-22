package io.github.sakurawald.fuji.core.config.handler.abst;

import com.google.gson.JsonObject;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.config.job.ConfigurationHandlerWriteStorageJob;
import io.github.sakurawald.fuji.core.config.mapper.GsonMapper;
import io.github.sakurawald.fuji.core.config.migrator.version.VersionPropertyInjector;
import io.github.sakurawald.fuji.core.config.migrator.transformer.abst.ConfigurationTransformer;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.document.interfaces.SourceModuleGetter;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.fuji.core.manager.impl.scheduler.ScheduleManager;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobDataMap;

@ForDeveloper("""
    1. Only use static inner class for a nested structure. (This is because a historical design problem in Java)
    2. If you want to register a new type for gson, just override the template method in module initializer.
    3. The type system of java is static, so you only need to give the object instance to gson. (A generic type can be passed via method parameter)
    4. Define configuration handler using static variable, to ensure it's unique.
    """)
public abstract class BaseConfigurationHandler<T> implements SourceModuleGetter {

    public static final Set<BaseConfigurationHandler<?>> REGISTERED_CONFIGURATION_HANDLERS = new HashSet<>();
    public static final String CONFIG_JSON_LITERAL = "config.json";

    /* File path and data model. */
    @Getter
    protected final @NotNull Path filePath;

    @Getter(lazy = true)
    private final T defaultModel = makeDefaultModel();

    @Setter(AccessLevel.PROTECTED)
    protected T model;

    private final List<Consumer<T>> preMappingModelIntoJsonObjectHooks = new ArrayList<>();
    private final List<Consumer<JsonObject>> postMappingModelIntoJsonObjectHooks = new ArrayList<>();
    private final List<Consumer<JsonObject>> preMappingJsonObjectIntoModelHooks = new ArrayList<>();
    private final List<Consumer<T>> postMappingJsonObjectIntoModelHooks = new ArrayList<>();

    private final List<ConfigurationTransformer> installedTransformers = new ArrayList<>();

    public BaseConfigurationHandler(@NotNull Path filePath) {
        this.filePath = filePath;
        this.addPostMappingModelIntoJsonObjectHook(VersionPropertyInjector::injectVersionProperty);
    }

    public BaseConfigurationHandler<T> addPreMappingModelIntoJsonObjectHook(@NotNull Consumer<T> hook) {
        this.preMappingModelIntoJsonObjectHooks.add(hook);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public BaseConfigurationHandler<T> addPostMappingModelIntoJsonObjectHook(@NotNull Consumer<JsonObject> hook) {
        this.postMappingModelIntoJsonObjectHooks.add(hook);
        return this;
    }

    public BaseConfigurationHandler<T> addPreMappingJsonObjectIntoModelHook(@NotNull Consumer<JsonObject> hook) {
        this.preMappingJsonObjectIntoModelHooks.add(hook);
        return this;
    }

    public BaseConfigurationHandler<T> addPostMappingJsonObjectIntoModelHook(@NotNull Consumer<T> hook) {
        this.postMappingJsonObjectIntoModelHooks.add(hook);
        return this;
    }

    public BaseConfigurationHandler<T> installTransformer(@NotNull ConfigurationTransformer transformer) {
        this.installedTransformers.add(transformer);
        return this;
    }

    public BaseConfigurationHandler<T> installTransformer(@NotNull Supplier<ConfigurationTransformer> transformerSupplier) {
        return installTransformer(transformerSupplier.get());
    }

    protected abstract T makeDefaultModel();

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public final void readStorage() {
        try {
            /* Apply transformers before read the storage. */
            this.installedTransformers.forEach(it -> {
                it.configure(this.filePath);
                it.apply();
            });

            /* Write default configuration into the storage, if file not exists. */
            if (Files.notExists(this.filePath)) {
                writeStorage();
            }

            /* Map *.json file into JsonObject instance. */
            @Cleanup Reader jsonReader = new BufferedReader(new InputStreamReader(new FileInputStream(this.filePath.toFile()), StandardCharsets.UTF_8));
            JsonObject jsonObject = GsonMapper.fromJson(jsonReader, JsonObject.class);

            /* Validate the JsonObject instance. */
            validateModel(jsonObject, getDefaultModelAsJsonTree());

            /* Map the JsonObject instance into the model typed T. */
            this.preMappingJsonObjectIntoModelHooks.forEach(hook -> hook.accept(jsonObject));
            this.model = (T) GsonMapper.fromJson(jsonObject, getDefaultModel().getClass());
            this.postMappingJsonObjectIntoModelHooks.forEach(hook -> hook.accept(model));

            /* Write storage at once, to:
             * 1. Keep the sync between memory and disk.
             * 2. Trigger the field naming conversion in gson.
             * */
            this.writeStorage();

            /* Register self. */
            REGISTERED_CONFIGURATION_HANDLERS.add(this);
        } catch (Exception e) {
            LogUtil.error("Failed to read configuration file {} from storage.", this.filePath, e);
            throw e;
        }
    }

    @SneakyThrows
    public final void writeStorage() {
        try {
            /* Ensure the model is initialized. */
            if (this.model == null) {
                this.model = this.getDefaultModel();
                LogUtil.debug("Write default configuration: {}", this.filePath.toFile().getAbsolutePath());
            }

            /* Map model T into JsonObject instance. */
            this.preMappingModelIntoJsonObjectHooks.forEach(hook -> hook.accept(this.model()));
            JsonObject jsonObject = this.getModelAsJsonTree();
            this.postMappingModelIntoJsonObjectHooks.forEach(hook -> hook.accept(jsonObject));

            /* Map JsonObject instance into *.json file. */
            Files.createDirectories(this.filePath.getParent());
            Files.writeString(this.filePath, GsonMapper.toJsonString(jsonObject));
        } catch (Exception e) {
            LogUtil.error("Failed to write configuration file {} into storage.", this.filePath, e);
            throw e;
        }
    }

    protected void validateModel(@NotNull JsonObject dataTree, @NotNull JsonObject schemaTree) {
        // no-op
    }

    public @NotNull JsonObject getModelAsJsonTree() {
        return GsonMapper.toJsonTree(this.model()).getAsJsonObject();
    }

    public @NotNull JsonObject getDefaultModelAsJsonTree() {
        return GsonMapper.toJsonTree(this.getDefaultModel()).getAsJsonObject();
    }

    public @NotNull T model() {
        /* Ensure the model is initialized. */
        if (this.model == null) {
            this.readStorage();
        }

        if (this.model == null) {
            throw new IllegalStateException("The model of configuration file %s is null".formatted(this.filePath));
        }

        return this.model;
    }

    @Override
    public String getSourceModule() {
        return ModuleManager.computeJoinedModulePath(this.model().getClass().getName());
    }

    @SneakyThrows
    public BaseConfigurationHandler<T> enableAutoSaveFeature() {
        /* Make and schedule the job. */
        String jobName = this.filePath.toFile().getCanonicalPath();
        ConfigurationHandlerWriteStorageJob writeStorageJob = new ConfigurationHandlerWriteStorageJob(jobName, new JobDataMap() {
            {
                // Specify the configuration handler instance.
                this.put(BaseConfigurationHandler.class.getName(), BaseConfigurationHandler.this);

                // Specify the source module.
                String sourceModuleInCurrentStackTrace = ReflectionUtil.Stacktrace.findSourceModuleInCurrentStackTrace();
                this.put(SourceModuleGetter.SPECIFIED_SOURCE_MODULE_KEY, sourceModuleInCurrentStackTrace);
            }
        }, () -> ScheduleManager.CRON_EVERY_TEN_SECONDS);
        Managers.getScheduleManager().scheduleJob(writeStorageJob);

        /* Write storage on server stopping. */
        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            LogUtil.debug("Write storage on server stopping: {}", this.filePath);
            this.writeStorage();
        });

        return this;
    }
}
