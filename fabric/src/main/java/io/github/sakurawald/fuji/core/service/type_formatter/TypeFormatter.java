package io.github.sakurawald.fuji.core.service.type_formatter;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TypeFormatter {

    public static @NotNull Text formatTypes(ServerCommandSource source, Map<String, Integer> type2amount) {
        MutableText ret = Text.empty();
        type2amount.forEach((k, v) -> {
            Text text = TextHelper.getTextByKey(source, "types.entry", v);
            text = TextHelper.Operators.replaceTextWithNamedArgument(text, "type", () -> Text.translatable(k));
            ret.append(text);
        });
        return ret;
    }

}
