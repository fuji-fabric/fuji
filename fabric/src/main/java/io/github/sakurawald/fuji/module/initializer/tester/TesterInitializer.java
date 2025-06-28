package io.github.sakurawald.fuji.module.initializer.tester;


import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import lombok.SneakyThrows;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;


@CommandNode("tester")
@CommandRequirement(level = 4)
public class TesterInitializer extends ModuleInitializer {

    public static int x = ModuleManager.evalOnEnable(()->3);

    @SneakyThrows(Exception.class)
    @CommandNode("run")
    private static int $run(@CommandSource ServerPlayerEntity player) {
//        LogUtil.info(LogUtil.findSourceModuleInCurrentStack());
        return 0;
    }

    @CommandNode("world")
    private static int testWorld(@CommandSource ServerPlayerEntity playerEntity) {
        MinecraftServer server = ServerHelper.getServer();
        server.getPlayerManager();
        return 1;
    }

    @CommandNode("visible")
    private static int testInvisible(@CommandSource ServerPlayerEntity player) {
        player.sendMessage(Text.of(String.valueOf(player.isInvisible())));
        return 1;
    }

    @CommandNode("text-replace")
    private static int testTextReplace(@CommandSource ServerPlayerEntity player) {
        testTextReplacement(player);
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
