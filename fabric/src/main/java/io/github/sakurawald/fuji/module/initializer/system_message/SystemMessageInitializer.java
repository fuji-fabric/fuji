package io.github.sakurawald.fuji.module.initializer.system_message;

import io.github.sakurawald.fuji.core.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.system_message.config.model.SystemMessageConfigModel;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableTextContent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Document("""
    Customize the `translatable text`, like most of `system messages`.
    """)
public class SystemMessageInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<SystemMessageConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, SystemMessageConfigModel.class);

    public static @Nullable MutableText modifyTranslatableText(String translatableKey, Object... args) {
        Map<String, String> key2value = config.model().key2value;
        if (key2value.containsKey(translatableKey)) {
            /* Prevent hijack too early. */
            if (ServerHelper.getServer() == null) {
                LogUtil.warn("Server is null currently, cannot hijack the translatable text with the key: {}", translatableKey);
                return null;
            }

            /* If the value is defined to `null`, then we ignore the modification at this point. And process it at sentMessageToClient(). */
            String value = key2value.get(translatableKey);
            if (value == null) {
                return null;
            }

            /* Replace with a new value. */
            TranslatableTextContent forceFallbackToSpecifiedValue = new TranslatableTextContent("force_fallback", value, args);
            String resolveArgumentsAsString = MutableText
                .of(forceFallbackToSpecifiedValue)
                .getString();
            MutableText newText = TextHelper.getTextByValue(null, resolveArgumentsAsString).copy();
            LogUtil.debug("Replace the translatable text {} with new value.", translatableKey);
            return newText;
        }

        // Return null, that means we will use the original value.
        return null;
    }
}
