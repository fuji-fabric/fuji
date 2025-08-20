package io.github.sakurawald.fuji.core.config.handler.abst;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.config.job.ConfigurationHandlerWriteStorageJob;
import io.github.sakurawald.fuji.core.config.structure.IgnoreModVersionStrategy;
import io.github.sakurawald.fuji.core.config.transformer.abst.ConfigurationTransformer;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.document.interfaces.SourceModuleGetter;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.fuji.core.manager.impl.scheduler.ScheduleManager;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobDataMap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ForDeveloper("""
    1. Only use static inner class for a nested structure. (This is because a historical design problem in Java)
    2. If you want to register a new type for gson, just override the template method in module initializer.
    3. The type system of java is static, so you only need to give the object instance to gson.
    4. Define configuration handler using static variable, to ensure it's unique.
    """)
public abstract class BaseConfigurationHandler<T> implements SourceModuleGetter {

    public static final String CONFIG_JSON = "config.json";

    public static final Set<BaseConfigurationHandler<?>> REGISTERED_CONFIGURATION_HANDLERS = new HashSet<>();
    public static final String MOD_VERSION_KEY = "MOD_VERSION";

    @Getter
    protected static Gson gson = new GsonBuilder()
        // The default naming policy is IDENTIFY, we need to ensure the naming style is consistent.
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        // Pretty print for readability.
        .setPrettyPrinting()
        // Pass through html characters, to support mini language.
        .disableHtmlEscaping()
        // Null-value is legal value, we should serialize it.
        .serializeNulls()
        // Exclude the mod version property in both serialization and de-serialization.
        .setExclusionStrategies(new IgnoreModVersionStrategy())
        // If the Gson can't find a no-args-constructor, then it will try to create an instance using Unsafe, and ignore all the declared field initializers.
        .disableJdkUnsafe()
        // Let's create it.
        .create();

    /* Json Path Parser. */
    private static ParseContext JSON_PATH_PARSER = null;

    /* File path and data model. */
    @Getter
    protected final @NotNull Path path;
    protected T model;

    private final List<ConfigurationTransformer> installedTransformers = new ArrayList<>();

    public BaseConfigurationHandler(@NotNull Path path) {
        this.path = path;
    }

    public static ParseContext getJsonPathParser() {
        if (JSON_PATH_PARSER == null) {
            configureJsonPathLibrary();
            JSON_PATH_PARSER = JsonPath.using(Configuration.defaultConfiguration());
        }

        return JSON_PATH_PARSER;
    }

    private static void configureJsonPathLibrary() {
        Configuration.setDefaults(new Configuration.Defaults() {

            @Override
            public JsonProvider jsonProvider() {
                return new GsonJsonProvider(gson);
            }

            @Override
            public MappingProvider mappingProvider() {
                return new GsonMappingProvider(gson);
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });
    }

    public static void registerGsonTypeAdapter(Type type, Object typeAdapter) {
        gson = gson
            .newBuilder()
            .registerTypeAdapter(type, typeAdapter)
            .create();
    }

    @SuppressWarnings("UnusedReturnValue")
    public BaseConfigurationHandler<T> installTransformer(@NotNull ConfigurationTransformer transformer) {
        this.installedTransformers.add(transformer);
        return this;
    }

    public BaseConfigurationHandler<T> installTransformer(@NotNull Supplier<ConfigurationTransformer> transformerSupplier) {
        return installTransformer(transformerSupplier.get());
    }

    protected abstract T getDefaultModel();

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void readStorage() {
        try {
            /* Apply transformers before read the storage. */
            this.installedTransformers.forEach(it -> {
                it.configure(this.path);
                it.apply();
            });

            /* Write default configuration into the storage, if file not exists. */
            if (Files.notExists(this.path)) {
                writeStorage();
            } else {
                // Merge data tree with schema tree: the gson.fromJson() will use default model as the schema tree, to generate missing default kv-pairs in data tree.
                @Cleanup Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.path.toFile()), StandardCharsets.UTF_8));
                T defaultModel = getDefaultModel();
                this.model = (T) gson.fromJson(reader, defaultModel.getClass());

                /* Write storage at once, to:
                 * 1. Keep the sync between memory and disk.
                 * 2. Trigger the field naming conversion in gson.
                 * */
                this.writeStorage();
            }

        } catch (Exception e) {
            LogUtil.error("Failed to read configuration file {} from storage.", this.path, e);
            throw e;
        }

        /* Register self. */
        REGISTERED_CONFIGURATION_HANDLERS.add(this);
    }

    protected void beforeWriteStorage() {
        // no-op
    }

    @SneakyThrows
    public final void writeStorage() {
        try {
            /* Ensure the model is initialized. */
            if (this.model == null) {
                this.model = this.getDefaultModel();
                LogUtil.debug("Write default configuration: {}", this.path.toFile().getAbsolutePath());
            }

            /* Call hook functions. */
            this.beforeWriteStorage();

            /* Serialize the Java object into Json tree. */
            JsonElement modelJsonTree = this.convertModelToJsonTree();
            beforeSerializeIntoString(modelJsonTree);
            String jsonString = gson.toJson(modelJsonTree);

            /* Write model to storage. */
            Files.createDirectories(this.path.getParent());
            Files.writeString(this.path, jsonString);
        } catch (Exception e) {
            LogUtil.error("Failed to write configuration file {} to disk.", this.path, e);
            throw e;
        }
    }

    private void beforeSerializeIntoString(@NotNull JsonElement jsonElement) {
        jsonElement.getAsJsonObject()
            .addProperty(MOD_VERSION_KEY, Fuji.MOD_VERSION);
    }

    public JsonElement convertModelToJsonTree() {
        return gson.toJsonTree(this.model());
    }

    @SneakyThrows
    @SuppressWarnings("SameParameterValue")
    private void scheduleWriteStorageJob(@NotNull String cron) {
        /* Make and schedule the job. */
        String jobName = this.path.toFile().getCanonicalPath();
        ConfigurationHandlerWriteStorageJob writeStorageJob = new ConfigurationHandlerWriteStorageJob(jobName, new JobDataMap() {
            {
                // Specify the configuration handler instance.
                this.put(BaseConfigurationHandler.class.getName(), BaseConfigurationHandler.this);

                // Specify the source module.
                String sourceModuleInCurrentStackTrace = ReflectionUtil.Stacktrace.findSourceModuleInCurrentStackTrace();
                this.put(SourceModuleGetter.SPECIFIED_SOURCE_MODULE_KEY, sourceModuleInCurrentStackTrace);
            }
        }, () -> cron);
        Managers.getScheduleManager().scheduleJob(writeStorageJob);

        /* Write storage on server stopping. */
        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            LogUtil.debug("Write storage on server stopping: {}", this.path);
            this.writeStorage();
        });
    }

    public BaseConfigurationHandler<T> enableAutoSaveFeature() {
        this.scheduleWriteStorageJob(ScheduleManager.CRON_EVERY_TEN_SECONDS);
        return this;
    }

    public @NotNull T model() {
        /* Ensure the model is initialized. */
        if (this.model == null) {
            this.readStorage();
        }

        if (this.model == null) {
            throw new IllegalStateException("The model of configuration file %s is null".formatted(this.path));
        }

        return this.model;
    }

    @Override
    public String getSourceModule() {
        return ModuleManager.computeJoinedModulePath(this.model().getClass().getName());
    }

}
