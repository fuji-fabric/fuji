package io.github.sakurawald.fuji.module.initializer.skin.structure;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.module.initializer.skin.SkinInitializer;
import io.github.sakurawald.fuji.module.initializer.skin.provider.MojangSkinProvider;
import io.github.sakurawald.fuji.module.initializer.skin.service.SkinService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;


public class SkinStorage {

    private static final Path skinStoragePath = ReflectionUtil.computeModuleConfigPath(SkinInitializer.class).resolve("skin-data");

    private static @NotNull Path computeFilePath(UUID playerUUID) {
        return skinStoragePath.resolve(playerUUID + ".json");
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean hasSkinData(@NotNull UUID playerUUID) {
        Path playerDataPath = computeFilePath(playerUUID);
        return Files.exists(playerDataPath);
    }

    public static void writeSkinData(@NotNull UUID playerUUID, @NotNull Property skinProperty) {
        try {
            Path playerDataPath = computeFilePath(playerUUID);
            String string = BaseConfigurationHandler.getGson().toJson(skinProperty);
            FileUtils.writeStringToFile(playerDataPath.toFile(), string, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LogUtil.error("Failed to save skin data for UUID {}.", playerUUID, e);
        }
    }

    public static Property readSkinData(GameProfile gameProfile) {
        UUID playerUUID = gameProfile.getId();
        /* Create new data. */
        if (!hasSkinData(playerUUID)) {
            createSkinData(gameProfile);
        }

        /* Read the data from the storage. */
        Path playerDataPath = computeFilePath(playerUUID);
        try {
            String string = Files.readString(playerDataPath);
            return BaseConfigurationHandler.getGson().fromJson(string, Property.class);
        } catch (IOException e) {
            LogUtil.error("Failed to load the skin data for player {}. (Fallback to default skin.)", gameProfile.getName(), e);
            return SkinService.getDefaultSkin();
        }
    }

    private static void createSkinData(GameProfile gameProfile) {
        String playerName = gameProfile.getName();
        UUID playerUUID = gameProfile.getId();
        LogUtil.info("There is not skin data for player {}. Creating new data now.", playerName);
        if (SkinInitializer.config.model().getDefaultSkin().isApplyDefaultSkinIfNoData()) {
            LogUtil.info("Create the new skin data for player {}. (Skin = specified default skin)", playerName);
            writeSkinData(playerUUID, SkinService.getDefaultSkin());
        } else {
            Optional<Property> mojangSkinProperty = MojangSkinProvider.fetchSkin(playerName);
            mojangSkinProperty.ifPresentOrElse($mojangSkinProperty -> {
                LogUtil.info("Create the new skin data for player {}. (Skin = Mojang online skin)", playerName);
                writeSkinData(playerUUID, $mojangSkinProperty);
            }, () -> LogUtil.info("Create the new skin data for player {}. (Skin = Failed to fetch Mojang online skin, fallback to default skin.)", playerName));
        }
    }

}
