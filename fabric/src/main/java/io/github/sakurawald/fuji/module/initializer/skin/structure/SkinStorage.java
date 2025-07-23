package io.github.sakurawald.fuji.module.initializer.skin.structure;

import com.mojang.authlib.properties.Property;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.module.initializer.skin.SkinInitializer;
import io.github.sakurawald.fuji.module.initializer.skin.service.SkinService;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class SkinStorage {

    private final Path skinStoragePath = ReflectionUtil.computeModuleConfigPath(SkinInitializer.class).resolve("skin-data");

    private final Map<UUID, Property> uuid2skin = new HashMap<>();

    private Path computeFilePath(UUID playerUUID) {
        return skinStoragePath.resolve(playerUUID + ".json");
    }

    public Property getSkin(UUID playerUUID) {
        if (!uuid2skin.containsKey(playerUUID)) {
            Property skin = this.readSkin(playerUUID);
            setSkin(playerUUID, skin);
        }

        return uuid2skin.get(playerUUID);
    }

    public void setSkin(UUID uuid, @Nullable Property skinProperty) {
        // if a player has no skin, use default skin.
        if (skinProperty == null) {
            skinProperty = SkinService.getDefaultSkin();
        }

        uuid2skin.put(uuid, skinProperty);
    }

    public void writeSkin(UUID uuid) {
        if (uuid2skin.containsKey(uuid)) {
            Property skin = uuid2skin.get(uuid);
            try {
                File file = computeFilePath(uuid).toFile();
                FileUtils.writeStringToFile(file, BaseConfigurationHandler.getGson().toJson(skin), StandardCharsets.UTF_8);
            } catch (IOException e) {
                LogUtil.error("Save skin failed: " + e.getMessage());
            }
        }
    }

    private @Nullable Property readSkin(UUID uuid) {
        Path playerData = this.computeFilePath(uuid);
        if (Files.notExists(playerData)) return null;

        try {
            String string = Files.readString(playerData);
            return BaseConfigurationHandler.getGson().fromJson(string, Property.class);
        } catch (IOException e) {
            LogUtil.error("Failed to load the skin for UUID {}.", uuid, e);
        }
        return null;
    }

}
