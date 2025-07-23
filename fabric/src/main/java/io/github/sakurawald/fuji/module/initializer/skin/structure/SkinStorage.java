package io.github.sakurawald.fuji.module.initializer.skin.structure;

import com.mojang.authlib.properties.Property;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.module.initializer.skin.SkinInitializer;
import io.github.sakurawald.fuji.module.initializer.skin.service.SkinService;
import java.util.Optional;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class SkinStorage {

    private final Path skinStoragePath = ReflectionUtil.computeModuleConfigPath(SkinInitializer.class).resolve("skin-data");

    private final Map<UUID, Property> skinCache = new HashMap<>();

    private Path computeFilePath(UUID playerUUID) {
        return skinStoragePath.resolve(playerUUID + ".json");
    }

    public Property getSkinCache(UUID playerUUID) {
        if (!skinCache.containsKey(playerUUID)) {
            Optional<Property> skinProperty = this.readSkinPreference(playerUUID);
            this.setSkinCache(playerUUID, skinProperty);
        }

        return skinCache.get(playerUUID);
    }

    public void setSkinCache(UUID playerUUID, Optional<Property> skinProperty) {
        // NOTE: If a player has not set any skin, then use the defined default skins for it.
        Property $skinProperty = skinProperty
            .orElse(SkinService.getRandomDefaultSkin());
        skinCache.put(playerUUID, $skinProperty);
    }

    public void writeSkinPreference(UUID playerUUID) {
        if (skinCache.containsKey(playerUUID)) {
            Property skinProperty = skinCache.get(playerUUID);
            try {
                Path playerDataPath = computeFilePath(playerUUID);
                String string = BaseConfigurationHandler.getGson().toJson(skinProperty);
                FileUtils.writeStringToFile(playerDataPath.toFile(), string, StandardCharsets.UTF_8);
            } catch (IOException e) {
                LogUtil.error("Failed to save skin preference for UUID {}.", playerUUID, e);
            }
        }
    }

    private Optional<Property> readSkinPreference(UUID playerUUID) {
        Path playerDataPath = this.computeFilePath(playerUUID);
        if (Files.notExists(playerDataPath)){
            return Optional.empty();
        }

        try {
            String string = Files.readString(playerDataPath);
            Property skinProperties = BaseConfigurationHandler.getGson().fromJson(string, Property.class);
            return Optional.ofNullable(skinProperties);
        } catch (IOException e) {
            LogUtil.error("Failed to load the skin for UUID {}.", playerUUID, e);
        }
        return Optional.empty();
    }

}
