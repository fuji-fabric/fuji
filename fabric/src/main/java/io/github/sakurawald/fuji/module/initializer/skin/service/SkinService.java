package io.github.sakurawald.fuji.module.initializer.skin.service;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.sakurawald.fuji.core.auxiliary.RandomUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.skin.SkinInitializer;
import io.github.sakurawald.fuji.module.initializer.skin.structure.SkinDescriptor;
import io.github.sakurawald.fuji.module.initializer.skin.structure.SkinRestorer;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class SkinService {


    public static int applySkin(@NotNull ServerCommandSource src, @NotNull Collection<GameProfile> targets, boolean setByOperator, @NotNull Supplier<Property> skinSupplier) {
        SkinRestorer.setSkinAsync(src.getServer(), targets, skinSupplier).thenAccept(pair -> {
            Collection<ServerPlayerEntity> players = pair.left();
            Collection<GameProfile> profiles = pair.right();

            if (profiles.isEmpty()) {
                TextHelper.sendTextByKey(src, "skin.action.failed");
                return;
            }

            /* feedback */
            if (setByOperator) {
                TextHelper.sendTextByKey(src, "skin.action.affected_profile", String.join(", ", profiles.stream().map(GameProfile::getName).toList()));

                if (!players.isEmpty()) {
                    TextHelper.sendTextByKey(src, "skin.action.affected_player", String.join(", ", players.stream().map(p -> p.getGameProfile().getName()).toList()));
                }
            } else {
                TextHelper.sendTextByKey(src, "skin.action.ok");
            }

        });

        return targets.size();
    }

    public static int applySkin(@NotNull ServerCommandSource src, @NotNull Supplier<Property> skinSupplier) {
        if (src.getPlayer() == null) return CommandHelper.Return.FAIL;

        return applySkin(src, Collections.singleton(src.getPlayer().getGameProfile()), false, skinSupplier);
    }

    public static Property getDefaultSkin() {
        return RandomUtil.drawList(SkinInitializer.config.model().getDefaultSkinList()).getSkinProperty();
    }

    public static boolean isDefaultSkin(GameProfile gameProfile) {
        Property textures = gameProfile.getProperties().get("textures").stream().findFirst().orElse(null);
        if (textures == null) return false;

        return SkinInitializer.config.model()
            .getDefaultSkinList()
            .stream()
            .map(SkinDescriptor::getSkinProperty)
            .anyMatch(it -> PlayerHelper.getPropertyValue(it).equals(PlayerHelper.getPropertyValue(textures)));
    }
}
