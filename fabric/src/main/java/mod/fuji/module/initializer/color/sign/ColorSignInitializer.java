package mod.fuji.module.initializer.color.sign;

import mod.fuji.core.config.mapper.GsonMapper;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.UuidHelper;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.core.service.attachment.AttachmentManager;
import mod.fuji.core.service.style_striper.StyleStriper;
import mod.fuji.core.structure.GlobalBlockPos;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.color.sign.config.model.ColorSignConfigModel;
import mod.fuji.module.initializer.color.sign.structure.SignCache;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

@Document(id = 1751824932882L, value = """
    This module allows you to use `color tags` in `sign blocks`.
    """)
@ColorBox(id = 1751900015107L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ You can write `style tags` on the sign.
    Foe example: `\\<red\\>`, `\\<rb\\>`, `\\<bold\\>` and `\\<i\\>`.
    """)
@TestCase(action = "Place a `sign block` and write style tags in it, then re-open the sign.", targets = "The style tags in the sign block should be `parsed` and `reversed`.")
public class ColorSignInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<ColorSignConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, ColorSignConfigModel.class);

    private static final String ATTACHMENT_SUBJECT = "color-sign-cache";


    public static @Nullable SignCache readSignCache(@NotNull GlobalBlockPos globalBlockPos) {
        String uuid = UuidHelper.getAttachedUuid(globalBlockPos);
        if (!AttachmentManager.existsAttachment(ATTACHMENT_SUBJECT, uuid)) {
            return null;
        }

        try {
            String data = AttachmentManager.getAttachment(ATTACHMENT_SUBJECT, uuid);
            return GsonMapper.fromJson(data, SignCache.class);
        } catch (IOException e) {
            LogUtil.error("Failed to read sign cache: spatialBlock = {}", globalBlockPos, e);
            return null;
        }
    }

    public static void writeSignCache(@NotNull GlobalBlockPos globalBlockPos, @NotNull SignCache signCache) {
        String uuid = UuidHelper.getAttachedUuid(globalBlockPos);
        String data = GsonMapper.toJsonString(signCache);
        try {
            AttachmentManager.setAttachment(ATTACHMENT_SUBJECT, uuid, data);
        } catch (IOException e) {
            LogUtil.error("Failed to write sign cache: spatialBlock = {}, signCache = {}", globalBlockPos, signCache, e);
        }
    }

    public static String stripeStyleTags(Player player, String string) {
        return StyleStriper.stripe(player, "sign", string);
    }
}
