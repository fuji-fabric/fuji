package io.github.sakurawald.module.initializer.head.privoder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.structure.Downloader;
import io.github.sakurawald.module.initializer.head.HeadInitializer;
import io.github.sakurawald.module.initializer.head.structure.Category;
import io.github.sakurawald.module.initializer.head.structure.Head;
import lombok.Cleanup;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
public class HeadProvider {

    private static final Path HEAD_DATA_DIR_PATH = ReflectionUtil.computeModuleConfigPath(HeadInitializer.class).resolve("head-data").toAbsolutePath();

    private static final String HEAD_DATABASE_API = "https://minecraft-heads.com/scripts/api.php?cat=%s&tags=true";

    @Getter(lazy = true)
    private static final Multimap<Category, Head> loadedHeads = fetchData();

    private static Path computePath(Category category) {
        return HEAD_DATA_DIR_PATH.resolve(category.name + ".json");
    }

    public static Multimap<Category, Head> fetchData() {
        HashMultimap<Category, Head> result = HashMultimap.create();

        for (Category category : Category.values()) {
            String URL = null;
            try {
                Path destination = computePath(category);

                // skip the downloading if file exists.
                if (Files.exists(destination)) {
                    loadCategory(result, category);
                    continue;
                }

                URL = HEAD_DATABASE_API.formatted(category.name);
                URI uri = URI.create(URL);
                Downloader downloader = new Downloader(uri.toURL(), destination) {
                    @Override
                    public void onComplete() {
                        loadCategory(result, category);
                    }
                };
                downloader.start();
            } catch (IOException e) {
                LogUtil.warn("failed to download heads from URL {}", URL);
            }
        }
        return result;
    }

    private static void loadCategory(HashMultimap<Category, Head> result, Category category) {
        try {
            LogUtil.debug("load head category: {}", category.name);

            Path path = computePath(category);
            @Cleanup InputStreamReader reader = new InputStreamReader(Files.newInputStream(path));

            JsonArray headsJson = JsonParser.parseReader(reader).getAsJsonArray();
            for (JsonElement headJson : headsJson) {
                try {
                    Head head = BaseConfigurationHandler.getGson().fromJson(headJson, Head.class);
                    result.put(category, head);
                } catch (Exception e) {
                    LogUtil.warn("invalid head: " + headJson);
                }
            }
        } catch (IOException e) {
            LogUtil.error("failed to load head category.", e);
        }
    }
}
