package io.github.sakurawald.core.structure;

import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import lombok.experimental.UtilityClass;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@UtilityClass
public class TypeFormatter {

    public static @NotNull Text formatTypes(ServerCommandSource source, Map<String, Integer> type2amount) {
        MutableText ret = Text.empty();
        type2amount.forEach((k, v) -> {
            Text text = TextHelper.getTextByKey(source, "types.entry", v);
            text = TextHelper.replaceTextWithMarker(text, "type", ()->Text.translatable(k));
            ret.append(text);
        });
        return ret;
    }

}
