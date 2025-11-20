package mod.fuji.core.service.type_formatter;

import mod.fuji.core.auxiliary.minecraft.TextHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TypeFormatter {

    public static @NotNull Component formatTypes(CommandSourceStack source, Map<String, Integer> type2amount) {
        MutableComponent ret = Component.empty();
        type2amount.forEach((k, v) -> {
            Component text = TextHelper.getTextByKey(source, "types.entry", v);
            text = TextHelper.Replacer.replaceTextWithNamedArgument(text, "type", (matcher) -> Component.translatable(k));
            ret.append(text);
        });
        return ret;
    }

}
