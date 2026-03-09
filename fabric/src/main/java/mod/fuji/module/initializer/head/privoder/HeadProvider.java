package mod.fuji.module.initializer.head.privoder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.ReflectionUtil;
import mod.fuji.core.config.mapper.GsonMapper;
import mod.fuji.core.structure.Downloader;
import mod.fuji.module.initializer.head.HeadInitializer;
import mod.fuji.module.initializer.head.structure.Category;
import mod.fuji.module.initializer.head.structure.Head;
import java.nio.charset.StandardCharsets;
import lombok.Cleanup;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class HeadProvider {

    private static final Path HEAD_DATA_DIR_PATH = ReflectionUtil.computeModuleConfigPath(HeadInitializer.class).resolve("head-data").toAbsolutePath();

    @Getter(lazy = true)
    private static final Multimap<Category, Head> loadedHeads = syncCategories();

    private static Path computePath(Category category) {
        return HEAD_DATA_DIR_PATH.resolve(category.name + ".json");
    }

    public static Multimap<Category, Head> syncCategories() {
        HashMultimap<Category, Head> result = HashMultimap.create();

        for (Category category : Category.values()) {
            String urlString = null;
            try {
                Path destination = computePath(category);

                // Skip download the category if it already exists.
                if (Files.exists(destination)) {
                    loadCategory(result, category);
                    continue;
                }

                // Download the specific category file.
                urlString = "https://minecraft-heads.com/scripts/api.php?cat=%s&tags=true".formatted(category.name);
                Downloader downloader = new Downloader(URI.create(urlString).toURL(), destination) {
                    @Override
                    public void onComplete() {
                        loadCategory(result, category);
                    }
                };
                downloader.startDownload();
            } catch (IOException e) {
                LogUtil.warn("Failed to download heads file from URL {}", urlString);
            }
        }
        return result;
    }

    private static void loadCategory(HashMultimap<Category, Head> result, Category category) {
        try {
            LogUtil.debug("Load head category: {}", category.name);

            Path path = computePath(category);
            @Cleanup InputStreamReader reader = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8);
            JsonArray headJsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            for (JsonElement headJsonElement : headJsonArray) {
                try {
                    Head head = GsonMapper.fromJson(headJsonElement, Head.class);
                    result.put(category, head);
                } catch (Exception e) {
                    LogUtil.error("Invalid head: {}", headJsonElement, e);
                }
            }
        } catch (Exception e) {
            LogUtil.error("Failed to load head category: {}", category, e);
        }
    }
}
