package io.github.sakurawald.fuji.module.initializer.tester;


import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandArgName;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.config.model.ConfigModel;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;

import java.util.Map;
import lombok.SneakyThrows;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

@Document(id = 1751980891153L, value = """
    This module is only used for `development`.
    If you are a developer, you can try new things here.
    You don't need to enable this module in production environment.
    It does not harm, but also not useful.
    """)
@CommandNode("tester")
@CommandRequirement(level = 4)
public class TesterInitializer extends ModuleInitializer {

    @SneakyThrows(Exception.class)
    @CommandNode("run")
    private static int $run(@CommandSource ServerCommandSource source, @CommandArgName("world") String hello) {

        LogUtil.info("Done");
        return 0;
    }

    private static void extracted(ServerCommandSource source, GreedyString commandLine) {
        CommandDispatcher<ServerCommandSource> commandDispatcher = CommandHelper.getCommandDispatcher();
        ParseResults<ServerCommandSource> parseResults = commandDispatcher.parse(commandLine.getValue(), source);
        Map<com.mojang.brigadier.tree.CommandNode<ServerCommandSource>, String> map = commandDispatcher.getSmartUsage(Iterables.getLast(parseResults.getContext().getNodes()).getNode(), source);
        for (String string : map.values()) {
            source.sendMessage(Text.literal("/" + parseResults.getReader().getString() + " " + string));
        }
    }

}
