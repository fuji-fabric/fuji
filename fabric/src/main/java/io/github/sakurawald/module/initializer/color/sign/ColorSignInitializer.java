package io.github.sakurawald.module.initializer.color.sign;

import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.UuidHelper;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.manager.Managers;
import io.github.sakurawald.core.structure.GlobalBlockPos;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.color.sign.config.model.ColorSignConfigModel;
import io.github.sakurawald.module.initializer.color.sign.structure.SignCache;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class ColorSignInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<ColorSignConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, ColorSignConfigModel.class);

    public static final String STYLE_TYPE_SIGN = "sign";

    private static final String ATTACHMENT_SUBJECT = "color-sign-cache";


    public static @Nullable SignCache readSignCache(@NonNull GlobalBlockPos globalBlockPos) {
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

    public static void writeSignCache(@NotNull GlobalBlockPos globalBlockPos, @NonNull SignCache signCache) {
        String uuid = UuidHelper.getAttachedUuid(globalBlockPos);
        String data = BaseConfigurationHandler.getGson().toJson(signCache);
        try {
            Managers.getAttachmentManager().setAttachment(ATTACHMENT_SUBJECT, uuid, data);
        } catch (IOException e) {
            LogUtil.error("Failed to write sign cache: spatialBlock = {}, signCache = {}", globalBlockPos, signCache, e);
        }
    }

}
