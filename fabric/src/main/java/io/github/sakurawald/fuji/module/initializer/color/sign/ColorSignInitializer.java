package io.github.sakurawald.fuji.module.initializer.color.sign;

import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.UuidHelper;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.service.style_striper.StyleStriper;
import io.github.sakurawald.fuji.core.structure.GlobalBlockPos;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.color.sign.config.model.ColorSignConfigModel;
import io.github.sakurawald.fuji.module.initializer.color.sign.structure.SignCache;

import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

@Document(id = 1751824932882L, value = """
    This module allows you to use color tags in `sign` blocks.
    """)
@ColorBox(id = 1751900015107L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    ◉ You can write `style tags` on the sign.
    Foe example: `\\<red\\>`, `\\<rb\\>`, `\\<bold\\>` and `\\<i\\>`.
    """)
@TestCase(action = "Place a `sign block` and write style tags in it, then re-open the sign.", targets = "The style tags in the sign block should be `parsed` and `reversed`.")
public class ColorSignInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<ColorSignConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, ColorSignConfigModel.class);

    private static final String ATTACHMENT_SUBJECT = "color-sign-cache";


    public static @Nullable SignCache readSignCache(@NotNull GlobalBlockPos globalBlockPos) {
        String uuid = UuidHelper.getAttachedUuid(globalBlockPos);
        if (!Managers.getAttachmentManager().existsAttachment(ATTACHMENT_SUBJECT, uuid)) {
            return null;
        }

        try {
            String data = Managers.getAttachmentManager().getAttachment(ATTACHMENT_SUBJECT, uuid);
            return BaseConfigurationHandler.getGson().fromJson(data, SignCache.class);
        } catch (IOException e) {
            LogUtil.error("Failed to read sign cache: spatialBlock = {}", globalBlockPos, e);
            return null;
        }
    }

    public static void writeSignCache(@NotNull GlobalBlockPos globalBlockPos, @NotNull SignCache signCache) {
        String uuid = UuidHelper.getAttachedUuid(globalBlockPos);
        String data = BaseConfigurationHandler.getGson().toJson(signCache);
        try {
            Managers.getAttachmentManager().setAttachment(ATTACHMENT_SUBJECT, uuid, data);
        } catch (IOException e) {
            LogUtil.error("Failed to write sign cache: spatialBlock = {}, signCache = {}", globalBlockPos, signCache, e);
        }
    }

    public static String stripeStyleTags(PlayerEntity player, String string) {
        return StyleStriper.stripe(player, "sign", string);
    }
}
