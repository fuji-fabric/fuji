package mod.fuji.module.initializer.tester.functions;

import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class TestFunctions {
    public static void testTextReplacement(ServerPlayer player) {
        /* make */
        MutableComponent root = Component.empty();

        MutableComponent first = Component.literal("first").withStyle(ChatFormatting.RED);
        root.append(first);

        MutableComponent first_first = Component.literal("second").withStyle(ChatFormatting.GREEN);
        first.append(first_first);

        MutableComponent first_second = Component.literal("third");
        first.append(first_second);

        /* replace */
        LogUtil.debug("before = {}", root);
        player.sendSystemMessage(root);

        MutableComponent after = TextHelper.Replacer.replaceTextWithRegex(root, "hi", (matcher) -> Component.literal("{replacement}"));
        LogUtil.debug("after = {}", after);
        player.sendSystemMessage(after);
    }
}
