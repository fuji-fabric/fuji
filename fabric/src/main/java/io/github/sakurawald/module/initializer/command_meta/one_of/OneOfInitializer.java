package io.github.sakurawald.module.initializer.command_meta.one_of;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.RandomUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.core.command.executor.CommandExecutor;
import io.github.sakurawald.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Arrays;
import java.util.List;

public class OneOfInitializer extends ModuleInitializer {

    @CommandNode("one-of")
    @CommandRequirement(level = 4)
    @Document("One-of command randomly pick one of commands and execute it as console.")
    private static int chain(@CommandSource ServerCommandSource source, GreedyString rest) {
        String $rest = rest.getValue();

        List<String> commands = Arrays.stream($rest.split("one-of"))
            .toList();

        String luckyCommand = RandomUtil
            .drawList(commands)
            .trim();

        LogUtil.debug("For {}, we pick the command {} to execute.", commands, luckyCommand);

        CommandExecutor.execute(ExtendedCommandSource.fromSource(source), luckyCommand);
        return CommandHelper.Return.SUCCESS;
    }
}
