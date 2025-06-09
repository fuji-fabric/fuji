package io.github.sakurawald.core.config.handler.abst;

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
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.config.job.ConfigurationHandlerSaverJob;
import io.github.sakurawald.core.config.transformer.abst.ConfigurationTransformer;
import io.github.sakurawald.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.core.manager.impl.scheduler.ScheduleManager;
import lombok.Cleanup;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobDataMap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
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

/**
 * I write some rules here to avoid forgetting.
 * 1. Only use static inner class in config model java object, this is because a historical design problem in java.
 * 2. The new gson type adapter should be registered before the call to loadFromDisk()
 * 3. The type system of java is static, given an object instance, you can use instance.getClass() to get the type of the instance, which means that you don't need to specify the typeOfT for gson library.
 *
 *
 * <p>
 * The configuration handler in module initializer should be static:
 * 1. The major point to make configuration handler a member of class, is that it's easier to control the lifecycle of objects, however, considering the fact that the configuration handler is a mapping between file system and memory, it should be static and unique.
 * 2. Create the instance of configuration handler should have no side effect, until the call to readStorage() and writeStorage()
 * <p>
 * Some other solutions:
 * 1. <a href="https://stackoverflow.com/questions/42503935/how-to-serialize-a-json-object-child-into-a-field">...</a>
 */
public abstract class BaseConfigurationHandler<T> {

    public static final String CONFIG_JSON = "config.json";

    public static final Set<BaseConfigurationHandler<?>> handlers = new HashSet<>();

    @Getter
    protected static Gson gson = new GsonBuilder()
        // the default naming policy is IDENTIFY, we ensure that the naming style is consistency, whatever the internal name is.
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        // for readability
        .setPrettyPrinting()
        // for mini-message language
        .disableHtmlEscaping()
        // null-value is legal value, we should serialize it.
        .serializeNulls()
        .create();

    /* json path */
    private static ParseContext jsonPathParser = null;
    @Getter
    protected final @NotNull Path path;
    /* transformer */
    private final List<ConfigurationTransformer> transformers = new ArrayList<>();
    protected T model;

    public BaseConfigurationHandler(@NotNull Path path) {
        this.path = path;
    }

    public static ParseContext getJsonPathParser() {
        if (jsonPathParser == null) {
            configureJsonPathLibrary();
            jsonPathParser = JsonPath.using(Configuration.defaultConfiguration());
        }

        return jsonPathParser;
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

    public static void registerTypeAdapter(Type type, Object typeAdapter) {
        gson = gson.newBuilder().registerTypeAdapter(type, typeAdapter).create();
    }

    public BaseConfigurationHandler<T> addTransformer(ConfigurationTransformer transformer) {
        this.transformers.add(transformer);
        return this;
    }

    protected abstract T getDefaultModel();

    @SuppressWarnings("unchecked")
    public void readStorage() {
        try {
            /* apply transformers first*/
            this.transformers.forEach(it -> {
                it.configure(this.path);
                it.apply();
            });

            if (Files.notExists(this.path)) {
                writeStorage();
            } else {
                // merge data tree with schema tree: the gson.fromJson() will use defaultModel as the schema tree to generate missing default kv-pairs for data tree.
                @Cleanup Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.path.toFile())));
                T defaultModel = getDefaultModel();
                this.model = (T) gson.fromJson(reader, defaultModel.getClass());

                /* write storage back, to:
                 * 1. keep the sync between memory and disk.
                 * 2. trigger the field naming conversion in gson.
                 * */
                this.writeStorage();
            }

        } catch (IOException e) {
            LogUtil.error("failed to read configuration file {} from disk.", this.path, e);
        }

        /* track this */
        handlers.add(this);
    }

    public void beforeWriteStorage() {
        // no-op
    }

    public void writeStorage() {
        try {
            // set default data tree
            if (this.model == null) {
                // getDefaultModel() is allowed to throw exception.
                this.model = this.getDefaultModel();
                LogUtil.info("write default configuration: {}", this.path.toFile().getAbsolutePath());
            }

            // before write storage
            this.beforeWriteStorage();

            // write data tree to disk
            Files.createDirectories(this.path.getParent());
            Files.writeString(this.path, gson.toJson(this.model));
        } catch (IOException e) {
            LogUtil.error("failed to write configuration file {} to disk.", this.path, e);
        }
    }

    public JsonElement convertModelToJsonTree() {
        // call model() instead of this.model to ensure the model is loaded.
        return gson.toJsonTree(this.model());
    }

    /**
     * This method exists for performance purpose.
     */
    @SuppressWarnings("SameParameterValue")
    private void scheduleWriteStorageJob(@NotNull String cron) {
        String jobName = this.path.toString();
        new ConfigurationHandlerSaverJob(jobName, new JobDataMap() {
            {
                this.put(BaseConfigurationHandler.class.getName(), BaseConfigurationHandler.this);
            }
        }, () -> cron).schedule();

        // write storage on server stopping.
        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            LogUtil.debug("write storage on server stopping: {}", this.path);
            this.writeStorage();
        });
    }

    public BaseConfigurationHandler<T> autoSaveEveryMinute() {
        this.scheduleWriteStorageJob(ScheduleManager.CRON_EVERY_MINUTE);
        return this;
    }

    public @NotNull T model() {
        // load storage if necessary.
        if (this.model == null) {
            this.readStorage();
        }

        if (this.model == null) {
            throw new IllegalStateException("The model of configuration file %s is null".formatted(this.path));
        }

        return this.model;
    }

}
