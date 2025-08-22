package io.github.sakurawald.fuji.core.config.parser;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import java.util.EnumSet;
import java.util.Set;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class JsonPathParser {

    @Getter(lazy = true)
    private static final ParseContext jsonPathParser = makeJsonPathParser();

    private static @NotNull ParseContext makeJsonPathParser() {
        configureJsonPathLibrary();
        return JsonPath.using(Configuration.defaultConfiguration());
    }

    private static void configureJsonPathLibrary() {
        Configuration.setDefaults(new Configuration.Defaults() {

            @Override
            public JsonProvider jsonProvider() {
                return new GsonJsonProvider(BaseConfigurationHandler.getGson());
            }

            @Override
            public MappingProvider mappingProvider() {
                return new GsonMappingProvider(BaseConfigurationHandler.getGson());
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });
    }
}
