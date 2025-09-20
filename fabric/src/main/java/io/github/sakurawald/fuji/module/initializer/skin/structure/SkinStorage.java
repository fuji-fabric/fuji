package io.github.sakurawald.fuji.module.initializer.skin.structure;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.AuthlibHelper;
import io.github.sakurawald.fuji.module.initializer.skin.SkinInitializer;
import io.github.sakurawald.fuji.core.service.gameprofile_fetcher.MojangSkinProvider;
import io.github.sakurawald.fuji.module.initializer.skin.service.SkinService;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;


public class SkinStorage {

    private static SkinDataNode getDefaultSkinDataNode(@NotNull GameProfile gameProfile) {
        String playerName = AuthlibHelper.getName(gameProfile);
        LogUtil.info("There is not skin data for player {}. Creating new data now.", playerName);

        if (SkinInitializer.config.model().getDefaultSkin().isApplyDefaultSkinIfNoData()) {
            LogUtil.info("Create the new skin data for player {}. (Skin = specified default skin)", playerName);
            return new SkinDataNode(playerName, PropertyWrapper.from(SkinService.getPreferredDefaultSkin()));
        } else {
            Optional<Property> mojangSkinProperty = MojangSkinProvider.fetchSkin(playerName);
            return mojangSkinProperty
                .map($mojangSkinProperty -> {
                    LogUtil.info("Create the new skin data for player {}. (Skin = Mojang online skin)", playerName);
                    return new SkinDataNode(playerName, PropertyWrapper.from($mojangSkinProperty));
                })
                .orElseGet(() -> {
                    LogUtil.info("Create the new skin data for player {}. (Skin = Failed to fetch Mojang online skin, fallback to the default skin.)", playerName);
                    return new SkinDataNode(playerName, PropertyWrapper.from(SkinService.getPreferredDefaultSkin()));
                });
        }
    }

    public static <T> T withSkinData(@NotNull GameProfile profile, @NotNull Function<SkinDataNode, T> function) {
        Optional<SkinDataNode> first = getSkinDataNodeList()
            .stream()
            .filter(it -> it.getPlayerName().equals(AuthlibHelper.getName(profile)))
            .findFirst();

        SkinDataNode $skinDataNode = first.orElseGet(() -> {
            SkinDataNode skinDataNode = getDefaultSkinDataNode(profile);
            getSkinDataNodeList().add(skinDataNode);
            return skinDataNode;
        });

        T apply = function.apply($skinDataNode);
        SkinInitializer.data.writeStorage();
        return apply;
    }

    private static List<SkinDataNode> getSkinDataNodeList() {
        return SkinInitializer.data.model().getNodes();
    }

}
