package mod.fuji.module.initializer.tester.functions;

import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TestFunctions {
    public static void testTextReplacement(ServerPlayerEntity player) {
        /* make */
        MutableText root = Text.empty();

        MutableText first = Text.literal("first").formatted(Formatting.RED);
        root.append(first);

        MutableText first_first = Text.literal("second").formatted(Formatting.GREEN);
        first.append(first_first);

        MutableText first_second = Text.literal("third");
        first.append(first_second);

        /* replace */
        LogUtil.debug("before = {}", root);
        player.sendMessage(root);

        MutableText after = TextHelper.Replacer.replaceTextWithRegex(root, "hi", (matcher) -> Text.literal("{replacement}"));
        LogUtil.debug("after = {}", after);
        player.sendMessage(after);
    }
}
