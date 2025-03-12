package io.github.sakurawald.module.initializer.tester;

import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.InventoryUtil;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import lombok.SneakyThrows;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;

@CommandNode("tester")
@CommandRequirement(level = 4)
public class TesterInitializer extends ModuleInitializer {

    private static final Map<String, String> map = new HashMap<>();

    @SneakyThrows(Exception.class)
    @CommandNode("run")
    private static int $run(@CommandSource ServerPlayerEntity player) {

        LogUtil.debug("main stacks = {}", InventoryUtil.getMainStacks(player));
        LogUtil.debug("armor = {}", InventoryUtil.getArmorStacks(player));

        return 1;
    }

    @CommandNode("visible")
    private static int testInvisible(@CommandSource ServerPlayerEntity player) {

        player.sendMessage(Text.of(String.valueOf(player.isInvisible())));

        return 1;
    }

    private static void testTextReplacement(ServerPlayerEntity player) {
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

        MutableText after = TextHelper.replaceTextWithRegex(root, "hi", () -> Text.literal("{replacement}"));
        LogUtil.debug("after = {}", after);
        player.sendMessage(after);
    }

    @CommandNode("$1 minus $2")
    private static int $2(@CommandSource ServerPlayerEntity player, Integer a, Integer b) {
        player.sendMessage(Text.of(String.valueOf(a - b)));
        return 1;
    }

    @CommandNode("ctx")
    private static int ctx(@CommandSource CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendMessage(Text.of("root"));
        return 1;
    }

    @CommandNode
    private static int root(@CommandSource ServerPlayerEntity player) {
        player.sendMessage(Text.of("root"));
        return 1;
    }
}
