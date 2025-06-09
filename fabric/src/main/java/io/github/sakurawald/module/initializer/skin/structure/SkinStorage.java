package io.github.sakurawald.module.initializer.skin.structure;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.RandomUtil;
import io.github.sakurawald.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.module.initializer.skin.SkinInitializer;
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

    private final Path rootPath = ReflectionUtil.computeModuleConfigPath(SkinInitializer.class).resolve("skin-data");

    private final Map<UUID, Property> uuid2skin = new HashMap<>();

    private Path computeFilePath(UUID uuid) {
        return rootPath.resolve(uuid + ".json");
    }

    public Property getDefaultSkin() {
        return RandomUtil.drawList(SkinInitializer.config.model().default_skins);
    }

    public boolean isDefaultSkin(GameProfile gameProfile) {
        Property textures = gameProfile.getProperties().get("textures").stream().findFirst().orElse(null);
        if (textures == null) return false;

        return SkinInitializer.config.model().default_skins.stream().anyMatch(it -> it.value().equals(textures.value()));
    }

    public Property getSkin(UUID uuid) {
        if (!uuid2skin.containsKey(uuid)) {
            Property skin = this.readSkin(uuid);
            setSkin(uuid, skin);
        }

        return uuid2skin.get(uuid);
    }

    public void setSkin(UUID uuid, @Nullable Property skin) {
        // if a player has no skin, use default skin.
        if (skin == null)
            skin = this.getDefaultSkin();

        uuid2skin.put(uuid, skin);
    }

    public void writeSkin(UUID uuid) {
        if (uuid2skin.containsKey(uuid)) {
            Property skin = uuid2skin.get(uuid);
            try {
                File file = computeFilePath(uuid).toFile();
                FileUtils.writeStringToFile(file, BaseConfigurationHandler.getGson().toJson(skin), StandardCharsets.UTF_8);
            } catch (IOException e) {
                LogUtil.error("save skin failed: " + e.getMessage());
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
            LogUtil.error("load skin failed: " + e.getMessage());
        }
        return null;
    }

}
