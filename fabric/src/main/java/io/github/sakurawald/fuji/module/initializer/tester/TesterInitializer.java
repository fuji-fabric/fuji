package io.github.sakurawald.fuji.module.initializer.tester;


import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.tester.functions.TestFunctions;

import lombok.SneakyThrows;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

@ColorBox(color = ColorBox.ColorBlockTypes.TIPS, value = """
  <json>
  "core": {
    "debug": {
      "disable_all_modules": false,
      "log_debug_messages": false,
      "print_user_guide_in_console": false
    },
    "backup": {
      "max_slots": 15,
      "skip": [
        "modules/head"
      ]
    },
  </json>

  """)

@CommandNode("tester")
@CommandRequirement(level = 4)
public class TesterInitializer extends ModuleInitializer {

    public static int x = ModuleManager.evalOnEnable(()->3);

    @SneakyThrows(Exception.class)
    @CommandNode("run")
    private static int $run(@CommandSource ServerPlayerEntity player) {

        TextHelper.sendTextByKey(player, "gui.search.title", "Hello");
        TextHelper.sendTextByKey(player, "gui.search.title", "Hello", "Second");
        TextHelper.sendTextByKey(player, "gui.search.title" );

        "Value = %s".formatted();


        return 0;
    }

    @CommandNode("text-replace")
    private static int testTextReplace(@CommandSource ServerPlayerEntity player) {
        TestFunctions.testTextReplacement(player);
        return 1;
    }

    @CommandNode("$1 minus $2")
    private static int $argumentReference(@CommandSource ServerPlayerEntity player, Integer a, Integer b) {
        player.sendMessage(Text.of(String.valueOf(a - b)));
        return 1;
    }

    @CommandNode("ctx")
    private static int $ctx(@CommandSource CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendMessage(Text.of("root"));
        return 1;
    }

    @CommandNode
    private static int $root(@CommandSource ServerPlayerEntity player) {
        player.sendMessage(Text.of("root"));
        return 1;
    }
}
